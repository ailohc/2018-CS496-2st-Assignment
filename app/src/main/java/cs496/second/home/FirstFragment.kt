package cs496.second.home

import android.support.v4.app.Fragment
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.FloatingActionButton
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.util.Pair
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import cs496.second.R

class FirstFragment : Fragment() {

    private var hashed_contact_list: HashMap<String, Contact>? = null
    private val contact_list: ArrayList<Contact>? = null
    private var name_list: ArrayList<String>? = null
    private var fileBtn: FloatingActionButton? = null
    private var fbBtn: FloatingActionButton? = null
    private var contact_search: EditText? = null
    private var contact_listview: ListView? = null
    private var contact_adapter: ContactAdapter? = null
    //for update
    private val UPDATE_SUCCESS = 1
    private val DATABASE_FAILURE = 2
    private val CONTACT_NOT_FOUND = 3
    private val FAILED_TO_UPDATE = 4
    //for delete
    private val SUCCESS = 1
    private val EXCEPTION_OCCURED = 3

    @Nullable
    @Override
    fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup, savedInstanceState: Bundle): View {
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)
        fileBtn = rootView.findViewById(R.id.fileBtn)
        fbBtn = rootView.findViewById(R.id.fbBtn)
        contact_search = rootView.findViewById(R.id.contact_search)
        contact_listview = rootView.findViewById(R.id.contact_listview)

        fileBtn!!.setOnClickListener(object : View.OnClickListener() {
            @Override
            fun onClick(view: View) {
                val localContact = GetContact()
                SendToServer(localContact).execute()
            }
        })

        contact_search!!.setRawInputType(InputType.TYPE_CLASS_TEXT)
        contact_search!!.setImeActionLabel("", EditorInfo.IME_ACTION_DONE)
        contact_search!!.setImeOptions(EditorInfo.IME_ACTION_DONE)
        contact_search!!.setOnEditorActionListener(object : TextView.OnEditorActionListener() {
            @Override
            fun onEditorAction(textview: TextView, Id: Int, event: KeyEvent?): Boolean {
                if (event == null) {
                    if (Id == EditorInfo.IME_ACTION_DONE) {
                        contact_search!!.clearFocus()
                        val inputMethodManager = getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(textview.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN)
                    } else if (Id == EditorInfo.IME_ACTION_NEXT) {
                    } else if (Id == EditorInfo.IME_ACTION_GO) {
                    } else {
                        return false
                    }
                } else if (Id == EditorInfo.IME_NULL) {
                    if (event!!.getAction() === KeyEvent.ACTION_DOWN) {
                    } else {
                        return true
                    }
                } else {
                    return false
                }
                return true
            }
        })
        contact_search!!.addTextChangedListener(object : TextWatcher() {
            @Override
            fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            @Override
            fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val query = contact_search!!.getText().toString().toLowerCase()
                contact_adapter!!.filter(query)
            }

            @Override
            fun afterTextChanged(editable: Editable) {
            }
        })

        contact_listview!!.setOnItemClickListener(object : AdapterView.OnItemClickListener() {
            @Override
            fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val intent = Intent(getActivity(), Contact_Detail_Activity::class.java)
                val name = adapterView.getAdapter().getItem(i).toString()
                intent.putExtra("name", name)
                intent.putExtra("phone_number", hashed_contact_list!!.get(name).phone)
                intent.putExtra("email", hashed_contact_list!!.get(name).email)
                intent.putExtra("facebook", hashed_contact_list!!.get(name).facebook)
                startActivity(intent)
            }
        })

        contact_listview!!.setOnItemLongClickListener(object : AdapterView.OnItemLongClickListener() {
            @Override
            fun onItemLongClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long): Boolean {
                val DeleteBtn = AlertDialog.Builder(getActivity())
                DeleteBtn.setMessage("Would you delete the item?").setCancelable(false).setPositiveButton("Confirm", object : DialogInterface.OnClickListener() {
                    @Override
                    fun onClick(dialogInterface: DialogInterface, j: Int) {
                        val name = adapterView.getAdapter().getItem(i).toString()
                        FindKeybyName(name, hashed_contact_list!!.get(name), FindKeybyName.DELETE).execute()
                    }
                }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener() {
                    @Override
                    fun onClick(dialogInterface: DialogInterface, j: Int) {
                    }
                })
                DeleteBtn.show()
                return true
            }
        })
        SynchronizeServer()

        return rootView
    }

    fun onActivityResult(request: Int, result: Int, data: Intent) {
        super.onActivityResult(request, result, data)
        SynchronizeServer()
    }

    fun GetContact(): HashMap<String, Contact> {
        val return_hashed = HashMap()
        val c = getActivity().getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc")
        while (c.moveToNext()) {
            val id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
            val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
            val contact = Contact(name)
            val cursorPhone = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null)
            if (cursorPhone.moveToFirst()) {
                contact.setPhone(cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
            }
            val cursorEmail = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null)
            if (cursorEmail.moveToFirst()) {
                contact.setEmail(cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)))
            }
            return_hashed.put(name, contact)
            cursorPhone.close()
            cursorEmail.close()
        }
        return return_hashed
    }

    fun UpdateServer(name: String, contact: Contact) {
        var name = name
        name = name.replace(' ', '+')
        FindKeybyName(name, contact, FindKeybyName.POST_OR_UPDATE).execute()
    }

    fun SynchronizeServer() {
        SynchronizeTask().execute()
    }

    private inner class SendToServer(private val UseHashMap: HashMap<String, Contact>) : AsyncTask() {
        @Override
        protected fun doInBackground(objects: Array<Object>): Object {
            val jsonResponse = ""
            try {
                val httpClient = DefaultHttpClient()
                val urlString = "http://52.231.66.244:8080/api/contacts/"
                val url = URI(urlString)
                val httpGet = HttpGet(url)
                val response = httpClient.execute(httpGet)
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

            return false
        }

        @Override
        protected fun onPostExecute(o: Object) {
            val checkConnection = o as Boolean
            if (checkConnection) {
                COUNTER = 0
                MAX = UseHashMap.size()
                for (entry in UseHashMap.entrySet()) {
                    UpdateServer(entry.getKey(), entry.getValue())
                }
            } else {
                Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class FindKeybyName(private val useName: String, private val useContact: Contact, private val next: Int) : AsyncTask() {
        @Override
        protected fun doInBackground(objects: Array<Object>): Object? {
            var jsonResponse = ""
            try {
                val httpClient = DefaultHttpClient()
                val urlString = "http://52.231.66.244:8080/api/contacts/name$useName"
                val url = URI(urlString)
                val httpGet = HttpGet(url)
                val response = httpClient.execute(httpGet)
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8)
                if (jsonResponse.contains("contact not found")) {
                    return null
                } else {
                    val arr = JSONArray(jsonResponse)
                    return arr.getJSONObject(0).getString("_id")
                }
            } catch (e: IOException) {
                getActivity().runOnUiThread(object : Runnable() {
                    @Override
                    fun run() {
                        Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show()
                    }
                })
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return null
        }

        @Override
        protected fun onPostExecute(o: Object?) {
            when (next) {
                POST_OR_UPDATE -> if (o == null) {
                    PostTask(useContact).execute()
                } else {
                    UpdateTask(o as String?, useContact).execute()
                }
                DELETE -> DeleteTask(o as String?).execute()
            }
        }

        companion object {
            val POST_OR_UPDATE = 1
            val DELETE = 2
        }
    }

    private inner class PostTask(private val PostContact: Contact) : AsyncTask() {

        @Override
        protected fun doInBackground(objects: Array<Object>): Object {
            var jsonResponse = ""
            try {
                val httpClient = DefaultHttpClient()
                val urlString = "http://52.231.66.244:8080/api/contacts/"
                val url = URI(urlString)
                val httpPost = HttpPost(url)
                val params = ArrayList()
                params.add(BasicNameValuePair("name", PostContact.name))
                params.add(BasicNameValuePair("phone", PostContact.phone))
                params.add(BasicNameValuePair("email", PostContact.email))
                params.add(BasicNameValuePair("facebook", PostContact.facebook))
                params.add(BasicNameValuePair("profileImage", PostContact.profileImage))
                val ent = UrlEncodedFormEntity(params, HTTP.UTF_8)
                httpPost.setEntity(ent)
                val response = httpClient.execute(httpPost)
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8)
                val obj = JSONObject(jsonResponse)
                return obj.getInt("result")
            } catch (e: IOException) {
                getActivity().runOnUiThread(object : Runnable() {
                    @Override
                    fun run() {
                        Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show()
                    }
                })
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return 0
        }

        @Override
        protected fun onPostExecute(o: Object) {
            if (o as Int == POST_SUCCESS) {
                COUNTER++
                if (COUNTER == MAX) SynchronizeServer()
            } else {
                Toast.makeText(getActivity(), "error to post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class UpdateTask(private val key: String, private val UpdateContact: Contact) : AsyncTask() {

        @Override
        protected fun doInBackground(objects: Array<Object>): Object? {
            var jsonResponse = ""
            try {
                val httpClient = DefaultHttpClient()
                val urlString = "http://52.231.66.244:8080/api/contacts/$key"
                val url = URI(urlString)
                val httpPut = HttpPut(url)
                val params = ArrayList()
                if (UpdateContact.name.equals("")) params.add(BasicNameValuePair("name", UpdateContact.name))
                if (UpdateContact.phone.equals("")) params.add(BasicNameValuePair("phone", UpdateContact.phone))
                if (UpdateContact.email.equals("")) params.add(BasicNameValuePair("email", UpdateContact.email))
                if (UpdateContact.facebook.equals("")) params.add(BasicNameValuePair("facebook", UpdateContact.facebook))
                if (UpdateContact.profileImage.equals("")) params.add(BasicNameValuePair("profileImage", UpdateContact.profileImage))
                val ent = UrlEncodedFormEntity(params, HTTP.UTF_8)
                httpPut.setEntity(ent)
                val response = httpClient.execute(httpPut)
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8)
                return if (jsonResponse.contains("update successful")) {
                    UPDATE_SUCCESS
                } else if (jsonResponse.contains("database failure")) {
                    DATABASE_FAILURE
                } else if (jsonResponse.contains("contact not found")) {
                    CONTACT_NOT_FOUND
                } else if (jsonResponse.contains("failed to update")) {
                    FAILED_TO_UPDATE
                } else {
                    null
                }

            } catch (e: IOException) {
                getActivity().runOnUiThread(object : Runnable() {
                    @Override
                    fun run() {
                        Toast.makeText(getActivity(), "Couldn't connect to Server", Toast.LENGTH_SHORT).show()
                    }
                })
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

            return null
        }

        @Override
        protected fun onPostExecute(o: Object?) {
            if (o == null) {
                Toast.makeText(getActivity(), "error to update", Toast.LENGTH_SHORT).show()
            } else {
                val code = o as Int
                if (code != UPDATE_SUCCESS) {
                    Toast.makeText(getActivity(), "error to update : $code", Toast.LENGTH_SHORT).show()
                } else {
                    COUNTER++
                    if (COUNTER == MAX) SynchronizeServer()
                }
            }
        }
    }

    private inner class DeleteTask(private val id: String) : AsyncTask() {

        @Override
        protected fun doInBackground(objects: Array<Object>): Object {
            val jsonResponse = ""
            try {
                val httpClient = DefaultHttpClient()
                val urlString = "http://52.231.66.244:8080/api/contacts/$id"
                val url = URI(urlString)
                val httpDelete = HttpDelete(url)
                val response = httpClient.execute(httpDelete)
                return if (response.getEntity() == null)
                    SUCCESS
                else
                    DATABASE_FAILURE
            } catch (e: IOException) {
                getActivity().runOnUiThread(object : Runnable() {
                    @Override
                    fun run() {
                        Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show()
                    }
                })
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

            return EXCEPTION_OCCURED
        }

        @Override
        protected fun onPostExecute(o: Object) {
            val code = o as Int
            if (code != SUCCESS) {
                Toast.makeText(getActivity(), "error to delete$code", Toast.LENGTH_SHORT).show()
            } else {
                SynchronizeServer()
            }
        }
    }

    private inner class SynchronizeTask : AsyncTask() {
        @Override
        protected fun doInBackground(objects: Array<Object>): Object {
            hashed_contact_list = HashMap<String, Contact>()
            var jsonResponse = ""
            try {
                val httpClient = DefaultHttpClient()
                val urlString = "http://52.231.66.244:8080/api/contacts/"
                val url = URI(urlString)
                val httpGet = HttpGet(url)
                val response = httpClient.execute(httpGet)
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8)
                val arr = JSONArray(jsonResponse)
                for (i in 0 until arr.length()) {
                    val obj_id = arr.getJSONObject(i).getString("_id")
                    var obj_name = arr.getJSONObject(i).getString("name")
                    val obj_phone = arr.getJSONObject(i).getString("phone")
                    val obj_email = arr.getJSONObject(i).getString("email")
                    val obj_facebook = arr.getJSONObject(i).getString("facebook")
                    val obj_profileImage = arr.getJSONObject(i).getString("profileImage")
                    obj_name = obj_name.replace('+', ' ')
                    hashed_contact_list!!.put(obj_name, Contact(obj_name, obj_phone, obj_email, obj_facebook, obj_profileImage))
                }
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return false
        }

        @Override
        protected fun onPostExecute(o: Object) {
            name_list = ArrayList(hashed_contact_list!!.keySet())
            contact_adapter = ContactAdapter(getActivity(), R.layout.contact_item, name_list)
            contact_listview!!.setAdapter(contact_adapter)
            if (o) {
                Toast.makeText(getActivity(), "Sucess", Toast.LENGTH_SHORT).show()
                Log.d("***********************", "Sucess to Update")
            } else
                Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        //for posting
        private val POST_SUCCESS = 1
        private var COUNTER: Int = 0
        private var MAX: Int = 0
    }
}
