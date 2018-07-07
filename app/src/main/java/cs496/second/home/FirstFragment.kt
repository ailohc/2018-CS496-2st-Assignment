package cs496.second.home

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cs496.second.R


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cs496.second.home.Adapter.ContactAdapter


class FirstFragment() : Fragment(), ContactAdapter.OnItemSelectedListener {


    companion object {
        fun newInstance(): FirstFragment {
            val fragment = FirstFragment()
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater!!.inflate(R.layout.fragment_first, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.contacts_recycler_view) as RecyclerView
        val adapter = ContactAdapter(contactsList!!)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        val formanage = LinearLayoutManager(activity)
        recyclerView.layoutManager = formanage
        recyclerView.setHasFixedSize(false)
        return rootView
    }

    override fun onItemSelected(selectedContact: Contact) {

        var intent = Intent(activity, ContactDetailsActivity::class.java)
        intent.putExtra("name", selectedContact.name)
        intent.putExtra("phone", selectedContact.phone)
        intent.putExtra("imageUrl", selectedContact.image.toString())
        startActivity(intent)

    }

}