package cs496.second.core.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cs496.second.core.dagger.inject
import cs496.second.core.flux.appComponent
import mini.DefaultSubscriptionTracker
import mini.Dispatcher
import mini.SubscriptionTracker
import javax.inject.Inject

abstract class FluxActivity : AppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject
    lateinit protected var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appComponent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}