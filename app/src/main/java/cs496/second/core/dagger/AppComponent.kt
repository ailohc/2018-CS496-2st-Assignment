package cs496.second.core.dagger

import android.app.Application
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import cs496.second.chat.store.ChatModule
import cs496.second.home.Third_Sub_Activity
import cs496.second.core.firebase.FirebaseModule
import cs496.second.core.flux.App
import cs496.second.core.flux.FluxActivity
import cs496.second.home.ThirdActivity
import cs496.second.profile.store.ProfileModule
import cs496.second.session.CreateAccountActivity
import cs496.second.session.EmailVerificationActivity
import cs496.second.session.LoginActivity
import cs496.second.session.store.SessionModule
import mini.Dispatcher
import mini.StoreMap

interface AppComponent {
    fun dispatcher(): Dispatcher
    fun stores(): StoreMap
}

@AppScope
@Component(modules = [(AppModule::class),
    (FirebaseModule::class),
    (ChatModule::class),
    (ProfileModule::class),
    (SessionModule::class)])

interface DefaultAppComponent : AppComponent {
    fun inject(target: Third_Sub_Activity)
    fun inject(target: FluxActivity)
    fun inject(target: LoginActivity)
    fun inject(target: CreateAccountActivity)
    fun inject(target: EmailVerificationActivity)
    fun inject(target: ThirdActivity)
}

@Module
class AppModule(val app: App) {
    @Provides
    @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app
}