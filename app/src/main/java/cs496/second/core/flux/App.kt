package cs496.second.core.flux

import android.app.Application
import cs496.second.BuildConfig
import cs496.second.core.dagger.AppComponent
import cs496.second.core.dagger.AppModule
import cs496.second.core.dagger.DaggerDefaultAppComponent
import cs496.second.util.Prefs
import mini.DebugTree
import mini.Grove
import mini.MiniActionReducer
import mini.log.LoggerInterceptor
import org.jetbrains.annotations.TestOnly
import kotlin.properties.Delegates

private var _app: App? = null

private var _prefs: Prefs by Delegates.notNull()
private var _appComponent: AppComponent? = null
val app: App get() = _app!!
val prefs: Prefs get() = _prefs
val appComponent: AppComponent get() = _appComponent!!

@TestOnly
fun setAppComponent(component: AppComponent) {
    _appComponent = component
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        _app = this
        _prefs = Prefs(this)

        if (BuildConfig.DEBUG) Grove.plant(DebugTree(true))


        if (_appComponent == null) {
            _appComponent = DaggerDefaultAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
            _appComponent!!.dispatcher().actionReducer = MiniActionReducer(stores = _appComponent!!.stores())
            _appComponent!!.dispatcher().addInterceptor(CustomLoggerInterceptor
            (_appComponent!!.stores().values))
        }
    }
}