package cs496.second.home;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cs496.second.R;

public class FirstFragment extends Fragment {

    private HashMap<String, Contact> localContact;
    private HashMap<String, Contact> hashed_contact_list;
    private ArrayList<Contact> contact_list;
    private ArrayList<String> name_list;
    private FloatingActionButton fileBtn;
    private FloatingActionButton deleteBtn;
    private EditText contact_search;
    private ListView contact_listview;
    private ContactAdapter contact_adapter;

    //for posting
    private static int POST_SUCCESS = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        fileBtn = rootView.findViewById(R.id.fileBtn);
        deleteBtn = rootView.findViewById(R.id.deleteBtn);
        contact_search = rootView.findViewById(R.id.contact_search);
        contact_listview = rootView.findViewById(R.id.contact_listview);
        localContact = GetContact();
        name_list = new ArrayList<>(localContact.keySet());
        contact_adapter = new ContactAdapter(getActivity(), R.layout.contact_item, name_list);
        contact_listview.setAdapter(contact_adapter);

        fileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SynchronizeServer();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {new DeleteTask().execute(); }
        });

        contact_search.setRawInputType(InputType.TYPE_CLASS_TEXT);
        contact_search.setImeActionLabel("", EditorInfo.IME_ACTION_DONE);
        contact_search.setImeOptions(EditorInfo.IME_ACTION_DONE);
        contact_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textview, int Id, KeyEvent event) {
                if (event == null) {
                    if (Id == EditorInfo.IME_ACTION_DONE) {
                        contact_search.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(textview.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    } else if (Id == EditorInfo.IME_ACTION_NEXT) {
                    } else if (Id == EditorInfo.IME_ACTION_GO) {
                    } else {
                        return false;
                    }
                } else if (Id == EditorInfo.IME_NULL) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
                return true;
            }
        });
        contact_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = contact_search.getText().toString().toLowerCase();
                contact_adapter.filter(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        contact_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), Contact_Detail_Activity.class);
                String name = adapterView.getAdapter().getItem(i).toString();
                intent.putExtra("name", name);
                intent.putExtra("phone", localContact.get(name).phone);
                intent.putExtra("email", localContact.get(name).email);
                startActivity(intent);
            }
        });

        contact_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder ShareBtn = new AlertDialog.Builder(getActivity());
                ShareBtn.setMessage("Would you share the contact?").setCancelable(false).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        String name = adapterView.getAdapter().getItem(i).toString();
                        new PostTask(localContact.get(name)).execute();
                    };
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                    }
                });
                ShareBtn.show();
                return true;
            }
        });

        return rootView;
    }

    public void onActivityResult (int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        SynchronizeServer();
    }

    public HashMap<String, Contact> GetContact() {
        HashMap<String, Contact> return_hashed = new HashMap<>();
        Cursor c = getActivity().getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            Contact contact = new Contact(name);
            Cursor cursorPhone = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                    null, null);
            if (cursorPhone.moveToFirst()) {
                contact.setPhone(cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }
            Cursor cursorEmail = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                    null, null);
            if (cursorEmail.moveToFirst()) {
                contact.setEmail(cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
            }
            return_hashed.put(name, contact);
            cursorPhone.close();
            cursorEmail.close();
        }
        return return_hashed;
    }


    public void SynchronizeServer() {
        new SynchronizeTask().execute();
    }

    private class PostTask extends AsyncTask {
        private Contact PostContact;
        public PostTask(Contact PostContact) {
            this.PostContact = PostContact;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            String jsonResponse = "";
            Log.d("***************", "aaaaaaaaaaaaaaaa");
            try {
                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://52.231.70.3:3000/api/contacts/";
                URI url = new URI(urlString);
                HttpPost httpPost = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("name"        , PostContact.name));
                params.add(new BasicNameValuePair("phone"       , PostContact.phone));
                params.add(new BasicNameValuePair("email"       , PostContact.email));
                params.add(new BasicNameValuePair("profileImage", PostContact.profileImage));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPost.setEntity(ent);
                HttpResponse response = httpClient.execute(httpPost);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                JSONObject obj = new JSONObject(jsonResponse);
                return obj.getInt("result");
            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
        @Override
        protected void onPostExecute(Object o) {
            if((int) o == POST_SUCCESS) {
                Toast.makeText(getActivity(), "success to post", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "error to post", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SynchronizeTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String jsonResponse = "";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://52.231.70.3:3000/api/contacts/";
                URI url = new URI(urlString);
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                JSONArray arr = new JSONArray(jsonResponse);
                int datalength = arr.length();
                for(int i = 0; i < arr.length(); i++) {
                    String obj_id           = arr.getJSONObject(i).getString("_id");
                    String obj_name         = arr.getJSONObject(i).getString("name");
                    String obj_phone        = arr.getJSONObject(i).getString("phone");
                    String obj_email        = arr.getJSONObject(i).getString("email");
                    String obj_profileImage = arr.getJSONObject(i).getString("profileImage");
                    obj_name = obj_name.replace('+', ' ');
                    localContact.put(obj_name, new Contact(obj_name, obj_phone, obj_email, obj_profileImage));
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            name_list = new ArrayList<>(localContact.keySet());
            contact_adapter = new ContactAdapter(getActivity(), R.layout.contact_item, name_list);
            contact_listview.setAdapter(contact_adapter);
            if((Boolean) o) {
                Toast.makeText(getActivity(), "Sucess to get data from Server", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show();
        }
    }

    private class DeleteTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String jsonResponse = "";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                String urlString = "http://52.231.70.3:3000/api/removeAll/";
                URI url = new URI(urlString);
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                JSONObject obj = new JSONObject(jsonResponse);
                return obj.getInt("result");
                } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Couldn't Connect to Server", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }
        @Override
        protected void onPostExecute(Object o) {
            if((int) o == POST_SUCCESS) {
                Toast.makeText(getActivity(), "success to init database", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "error to init database", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
