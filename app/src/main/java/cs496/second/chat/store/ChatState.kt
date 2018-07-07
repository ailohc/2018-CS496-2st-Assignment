package cs496.second.chat.store

import cs496.second.chat.model.Message
import cs496.second.util.Task
import io.reactivex.disposables.CompositeDisposable

data class ChatState(val messages: Map<String, Message> = emptyMap(),
                     val sendMessageTask : Task = Task(),
                     val disposables: CompositeDisposable = CompositeDisposable())