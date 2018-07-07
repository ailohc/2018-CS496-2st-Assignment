package cs496.second.profile.store

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import cs496.second.chat.store.SendMessageCompleteAction
import cs496.second.core.dagger.AppScope
import cs496.second.profile.controller.ProfileController
import cs496.second.profile.controller.ProfileControllerImpl
import cs496.second.session.store.CreateAccountCompleteAction
import cs496.second.session.store.LoginCompleteAction
import cs496.second.util.taskRunning
import mini.Reducer
import mini.Store
import javax.inject.Inject

@AppScope
class ProfileStore @Inject constructor(val controller: ProfileController) : Store<ProfileState>() {

    @Reducer
    fun loadAndCreateUserData(action: CreateAccountCompleteAction, state: ProfileState): ProfileState {
        if (!action.task.isSuccessful()) return state
        controller.loadUserProfile(action.user!!) //User can't be null if the request is successful
        return state.copy(loadProfileTask = taskRunning())
    }

    @Reducer
    fun loadUserOnLogin(action: LoginCompleteAction, state: ProfileState): ProfileState {
        if (!action.task.isSuccessful()) return state
        controller.loadUserProfile(action.user!!) //User can't be null if the request is successful
        return state.copy(loadProfileTask = taskRunning())
    }

    fun updateMessageCount(action: SendMessageCompleteAction, state: ProfileState): ProfileState {
        if (!action.task.isSuccessful()) return state
        return state.copy(publicProfile = state.publicProfile?.copy(totalMessages = state.publicProfile.totalMessages.plus(1)))
    }

    @Reducer
    fun userDataLoaded(action: LoadUserDataCompleteAction, state: ProfileState): ProfileState {
        if (!state.loadProfileTask.isRunning()) return state
        return state.copy(loadProfileTask = action.task, publicProfile = action.publicProfile, privateData = action.privateData)
    }
}

@Module
abstract class ProfileModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(ProfileStore::class)
    abstract fun provideProfileStore(store: ProfileStore): Store<*>

    @Binds
    @AppScope
    abstract fun bindProfileController(impl: ProfileControllerImpl): ProfileController
}
