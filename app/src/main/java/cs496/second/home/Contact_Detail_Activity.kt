package cs496.second.home

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import cs496.second.R
import kotlinx.android.synthetic.main.contact_detail.*
import org.jetbrains.anko.makeCall
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sendSMS
import org.jetbrains.anko.toast

class Contact_Detail_Activity : AppCompatActivity() {

    private var nameText: String? = null
    private var phoneText: String? = null
    private var emailText: String? = null
    private var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        nameText = intent.getStringExtra("name")
        phoneText = intent.getStringExtra("phone")
        emailText = intent.getStringExtra("email")
        tv_name.text = nameText
        tv_phone.text = phoneText
        tv_email.text = emailText
        phoneNumber = tv_phone.text.toString()

        tv_name.onClick {
            toast(tv_name.text)
        }

        tv_phone.onClick {
            toast(tv_phone.text)
        }

        val callButton = findViewById<Button>(R.id.button1)
        callButton.setOnClickListener {
            makeCall(phoneNumber!!)
        }

        val messageButton = findViewById<Button>(R.id.button2)
        messageButton.setOnClickListener {
            sendSMS(phoneNumber!!, "")
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home)
            finish()
        return true
    }

}



