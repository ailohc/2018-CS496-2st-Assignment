package cs496.second.chat.store

import cs496.second.chat.model.Message
import cs496.second.util.Task
import io.reactivex.disposables.Disposable
import mini.Action

class StartListeningChatMessagesAction : Action

data class ListeningChatMessagesCompleteAction(val disposable: Disposable) : Action

class MessagesLoadedAction(val messages: List<Message>) : Action

class StopListeningChatMessagesAction : Action

data class SendMessageAction(val message: String) : Action

data class SendMessageCompleteAction(val message : Message?, val task: Task) : Action