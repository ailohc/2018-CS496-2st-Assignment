package cs496.second.home

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import cs496.second.core.flux.FluxActivity
import cs496.second.home.ThirdActivity
import cs496.second.session.LoginActivity
import cs496.second.session.store.SessionStore
import cs496.second.session.store.TryToLoginInFirstInstanceAction
import cs496.second.util.filterOne
import cs496.second.util.toast
import javax.inject.Inject

class Third_Sub_Activity : FluxActivity() {

    @Inject
    lateinit var sessionStore: SessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val intent = ThirdActivity.newIntent(this)
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
