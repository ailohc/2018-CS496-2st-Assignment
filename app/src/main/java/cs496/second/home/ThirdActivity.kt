package cs496.second.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import cs496.second.R
import cs496.second.chat.store.ChatStore
import cs496.second.chat.store.SendMessageAction
import cs496.second.chat.store.StartListeningChatMessagesAction
import cs496.second.chat.store.StopListeningChatMessagesAction
import cs496.second.core.flux.FluxActivity
import cs496.second.profile.store.ProfileStore
import cs496.second.session.LoginActivity
import cs496.second.util.*
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.android.synthetic.main.third_activity.*
import javax.inject.Inject

class ThirdActivity : FluxActivity() {

    @Inject
    lateinit var profileStore: ProfileStore
    @Inject
    lateinit var chatStore: ChatStore

    companion object {
        fun newIntent(context: Context): Intent =
                Intent(context, ThirdActivity::class.java)
    }

    private val messageAdapter = MessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.third_activity)
        initializeInterface()
        startListeningStoreChanges()
    }

    private fun initializeInterface() {
        messageRecycler.setLinearLayoutManager(this, reverseLayout = false, stackFromEnd = false)
        messageRecycler.adapter = messageAdapter
        sendButton.setOnClickListener { sendMessage() }
    }

    private fun startListeningStoreChanges() {
        profileStore.flowable()
                .view { it.loadProfileTask }
                .subscribe {
                    when (it.status) {
                        TypedTask.Status.RUNNING -> showProgressDialog("Loading user profile")
                        TypedTask.Status.SUCCESS -> dismissProgressDialog()
                        TypedTask.Status.FAILURE -> goToLogin()
                    }
                }.track()

        chatStore.flowable()
                .view { it.messages }
                .filter { it.isNotEmpty() }
                .subscribe { messageAdapter.updateMessages(it.values.toList()) }
                .track()
    }

    private fun sendMessage() {
        if (messageEditText.text.isEmpty()) {
            toast("You should add a text")
            return
        }
        sendButton.isEnabled = false
        dispatcher.dispatchOnUi(SendMessageAction(messageEditText.text.toString()))
        messageEditText.text.clear()
        chatStore.flowable()
                .filterOne { it.sendMessageTask.isTerminal() } //Wait for request to finish
                .subscribe {
                    if (it.sendMessageTask.isFailure()) {
                        toast("There was an error sending your message")
                    }
                    sendButton.isEnabled = true
                }.track()
    }

    private fun goToLogin() {
        dismissProgressDialog()
        val intent = LoginActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        dispatcher.dispatch(StartListeningChatMessagesAction())
    }

    override fun onStop() {
        super.onStop()
        dispatcher.dispatch(StopListeningChatMessagesAction())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = HomeActivity.newIntent(this)
        startActivity(intent)
    }
}