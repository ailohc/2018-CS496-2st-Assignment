package cs496.second.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import cs496.second.home.HomeActivity
import cs496.second.R
import cs496.second.core.flux.FluxActivity
import cs496.second.session.store.SendVerificationEmailAction
import cs496.second.session.store.SessionStore
import cs496.second.session.store.SignOutAction
import cs496.second.session.store.VerifyUserEmailAction
import cs496.second.util.dismissProgressDialog
import cs496.second.util.filterOne
import cs496.second.util.showProgressDialog
import cs496.second.util.toast
import kotlinx.android.synthetic.main.verification_email_activity.*
import javax.inject.Inject

class EmailVerificationActivity : FluxActivity() {

    @Inject
    lateinit var sessionStore: SessionStore

    private lateinit var verificationEmail: String

    companion object {
        val VERIFICATION_EMAIL = "verification_email"
        fun startActivity(context: Context, email: String) {
            val intent = Intent(context, EmailVerificationActivity::class.java)
                .apply {
                    putExtra(VERIFICATION_EMAIL, email)
                }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification_email_activity)
        if (!intent.hasExtra(VERIFICATION_EMAIL)) {
            throw IllegalAccessException("verification email needed")
        }
        verificationEmail = intent.getStringExtra(VERIFICATION_EMAIL)
        initializeInterface()
    }

    private fun initializeInterface() {
        confirm_email_next_button.setOnClickListener { verifyUserEmail() }
        confirm_email_re_send_button.setOnClickListener { sendVerificationEmailAction() }
    }

    private fun verifyUserEmail() {
        confirm_email_next_button.isEnabled = false
        showProgressDialog("Verifying")
        dispatcher.dispatchOnUi(VerifyUserEmailAction())
        sessionStore.flowable()
            .filterOne { it.verifyUserTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.verifyUserTask.isSuccessful() && it.loggedUser != null && it.verified) {
                    goHome()
                } else {
                    toast("Your account hasn´t been confirmed yet")
                }
                confirm_email_next_button.isEnabled = true
                dismissProgressDialog()
            }.track()
    }

    private fun sendVerificationEmailAction() {
        showProgressDialog("Sending notification email")
        dispatcher.dispatch(SendVerificationEmailAction())
        sessionStore.flowable()
            .filterOne { it.verificationEmailTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.verificationEmailTask.isSuccessful()) {
                    toast("We have sent a verification email to your address")
                } else {
                    it.verificationEmailTask.error!!.message?.let { toast(it) }
                }
                dismissProgressDialog()
            }.track()
    }

    private fun goHome() {
        dismissProgressDialog()
        val intent = HomeActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        dispatcher.dispatchOnUi(SignOutAction())
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}