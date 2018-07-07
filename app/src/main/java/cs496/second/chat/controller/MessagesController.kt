package cs496.second.chat.controller

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import cs496.second.chat.store.ListeningChatMessagesCompleteAction
import cs496.second.chat.store.MessagesLoadedAction
import cs496.second.chat.store.SendMessageCompleteAction
import cs496.second.core.dagger.AppScope
import cs496.second.core.firebase.*
import cs496.second.core.firebase.FirebaseConstants.TOTAL_MESSAGES
import cs496.second.core.flux.doAsync
import cs496.second.profile.model.PublicProfile
import cs496.second.util.taskFailure
import cs496.second.util.taskSuccess
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import mini.Dispatcher
import javax.inject.Inject

interface ChatController {
    fun startListeningMessages()
    fun sendMessage(message: String, publicProfile: PublicProfile)
}

@AppScope
class ChatControllerImpl @Inject constructor(private val firestore: FirebaseFirestore,
                                             private val dispatcher: Dispatcher) : ChatController {
    override fun startListeningMessages() {
        val disposable = listenMessagesFlowable()
                .map { it.documents.map { it.toMessage() } }
                .subscribeOn(Schedulers.io())
                .subscribe { dispatcher.dispatchOnUi(MessagesLoadedAction(it)) }
        dispatcher.dispatchOnUi(ListeningChatMessagesCompleteAction(disposable))
    }

    override fun sendMessage(message: String, publicProfile: PublicProfile) {
        doAsync {
            val newId = firestore.messages().document().id
            val firebaseMessage = FirebaseMessage(publicProfile.userData.toFirebaseUserData(), message)

            try {
                val batch = firestore.batch()
                batch.set(firestore.messageDoc(newId), firebaseMessage)
                batch.update(firestore.publicProfileDoc(publicProfile.userData.uid),
                        mapOf(TOTAL_MESSAGES to publicProfile.totalMessages.plus(1)))
                Tasks.await(batch.commit())

                dispatcher.dispatchOnUi(SendMessageCompleteAction(firebaseMessage.toMessage(newId), taskSuccess()))
            } catch (e: Throwable) {
                dispatcher.dispatchOnUi(SendMessageCompleteAction(null, taskFailure(e)))
            }
        }
    }

    private fun listenMessagesFlowable(): Flowable<QuerySnapshot> {
        return Flowable.create({ emitter ->
            val registration = firestore.messages().addSnapshotListener({ documentSnapshot, e ->
                if (e != null && !emitter.isCancelled) {
                    emitter.onError(e)
                } else if (documentSnapshot != null) {
                    emitter.onNext(documentSnapshot)
                }
            })
            emitter.setCancellable { registration.remove() }
        }, BackpressureStrategy.BUFFER)
    }
}