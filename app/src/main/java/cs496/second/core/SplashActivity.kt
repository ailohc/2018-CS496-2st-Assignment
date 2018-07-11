package cs496.second.core

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import cs496.second.R
import cs496.second.core.flux.FluxActivity
import cs496.second.home.HomeActivity
import cs496.second.home.ThirdActivity
import cs496.second.session.LoginActivity
import cs496.second.session.store.SessionStore
import cs496.second.session.store.TryToLoginInFirstInstanceAction
import cs496.second.util.filterOne
import cs496.second.util.toast
import javax.inject.Inject

class SplashActivity : FluxActivity() {

    @Inject
    lateinit var sessionStore: SessionStore
    val MULTIPLE_PERMISSIONS = 10;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)
        val permissions = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
        ActivityCompat.requestPermissions(this, permissions, MULTIPLE_PERMISSIONS)
        Thread.sleep(5000)
        if (checkPlayServices()) {
            dispatcher.dispatch(TryToLoginInFirstInstanceAction())
            sessionStore.flowable()
                    .filterOne { it.loginTask.isTerminal() }
                    .subscribe { status ->
                        if (status.loginTask.isSuccessful()) goToHome()
                        else goToOnLogin()
                    }.track()
        }
    }

    private fun goToHome() {
        val intent = HomeActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun goToOnLogin() {
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 666).show()
            } else {
                toast("Google play is not supported in this device")
                finish()
            }
            return false
        }
        return true
    }
}
