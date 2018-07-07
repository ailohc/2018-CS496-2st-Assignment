package cs496.second.chat.store

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import cs496.second.chat.controller.ChatController
import cs496.second.chat.controller.ChatControllerImpl
import cs496.second.core.dagger.AppScope
import cs496.second.profile.store.ProfileStore
import cs496.second.session.store.SignOutAction
import cs496.second.util.taskRunning
import io.reactivex.disposables.CompositeDisposable
import mini.Reducer
import mini.Store
import javax.inject.Inject

@AppScope
class ChatStore @Inject constructor(val controller: ChatController, val profileStore: ProfileStore) : Store<ChatState>() {

    @Reducer
    fun loadMessages(action: StartListeningChatMessagesAction, state: ChatState): ChatState {
        controller.startListeningMessages()
        return state
    }

    @Reducer
    fun messagesReceived(action: MessagesLoadedAction, state: ChatState): ChatState {
        return state.copy(messages = state.messages.plus(action.messages.map { it.uid to it }.toMap()))
    }

    @Reducer
    fun sendMessage(action: SendMessageAction, state: ChatState): ChatState {
        controller.sendMessage(action.message, profileStore.state.publicProfile!!)
        return state.copy(sendMessageTask = taskRunning())
    }

    @Reducer
    fun messageSent(action: SendMessageCompleteAction, state: ChatState): ChatState {
        return state.copy(sendMessageTask = action.task,
                messages = if (action.task.isSuccessful()) state.messages
                        .plus(action.message!!.uid to action.message) else state.messages)
    }

    @Reducer
    fun stopListeningMessages(action: StopListeningChatMessagesAction, state: ChatState): ChatState {
        state.disposables.dispose()
        return state.copy(disposables = CompositeDisposable())
    }

    @Reducer
    fun signOut(action: SignOutAction, state: ChatState): ChatState {
        return initialState()
    }
}

@Module
abstract class ChatModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(ChatStore::class)
    abstract fun provideChatStore(store: ChatStore): Store<*>

    @Binds
    @AppScope
    abstract fun bindChatController(impl: ChatControllerImpl): ChatController
}
