package cs496.second.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse
import com.ibm.watson.developer_cloud.http.HttpMediaType
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice
import cs496.second.R

import java.util.ArrayList
import java.util.HashMap


class FourthActivity : AppCompatActivity() {


    private var recyclerView: RecyclerView? = null
    private var mAdapter: ChatAdapter? = null
    private var messageArrayList: ArrayList<Message>? = null
    private var inputMessage: EditText? = null
    private var btnSend: ImageButton? = null
    private var context: MutableMap<String, Any> = HashMap()
    internal var streamPlayer: StreamPlayer = StreamPlayer()
    private var initialRequest: Boolean = false
    private var permissionToRecordAccepted = false
    private var listening = false
    private var speechService: SpeechToText? = null
    private var capture: MicrophoneInputStream? = null
    private val recoTokens: SpeakerLabelsDiarization.RecoTokens? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourth)

        inputMessage = findViewById<EditText>(R.id.message) as EditText
        btnSend = findViewById<ImageButton>(R.id.btn_send) as ImageButton
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view) as RecyclerView

        messageArrayList = ArrayList()
        mAdapter = ChatAdapter(messageArrayList as ArrayList<Message>)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator() as RecyclerView.ItemAnimator?
        recyclerView!!.adapter = mAdapter
        this.inputMessage!!.setText("")
        this.initialRequest = true
        sendMessage()

        //Watson Text-to-Speech Service on Bluemix
        val service = TextToSpeech()
        service.setUsernameAndPassword("3b1d0f45-47da-4bd7-b95d-e161f455dcaf", "PZ8xwGamhMxZ")

        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }


        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView as RecyclerView, object : ClickListener {
            override fun onClick(view: View, position: Int) {
                val thread = Thread(Runnable {
                    val audioMessage: Message?
                    try {

                        audioMessage = messageArrayList!![position]
                        streamPlayer = StreamPlayer()
                        if (audioMessage.message.length>0)
                        //Change the Voice format and choose from the available choices
                            streamPlayer.playStream(service.synthesize(audioMessage.message, Voice.EN_LISA).execute())
                        else
                            streamPlayer.playStream(service.synthesize("No Text Specified", Voice.EN_LISA).execute())

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
                thread.start()
            }

            override fun onLongClick(view: View, position: Int) {

            }
        }))

        btnSend!!.setOnClickListener {
                sendMessage()
        }
    }

    // Speech to Text Record Audio permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            RECORD_REQUEST_CODE -> {

                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return
            }
        }
    }

    protected fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_REQUEST_CODE)
    }

    // Sending a message to Watson Conversation Service
    private fun sendMessage() {

        val inputmessage = this.inputMessage!!.text.toString();//.text.toString() { it <= ' ' }
        if (!this.initialRequest) {
            val inputMessage = Message()
            inputMessage.message = inputmessage
            inputMessage.id = "1"
            messageArrayList!!.add(inputMessage)
        } else {
            val inputMessage = Message()
            inputMessage.message = inputmessage
            inputMessage.id = "100"
            this.initialRequest = false
        }

        this.inputMessage!!.setText("")
        mAdapter!!.notifyDataSetChanged()

        val thread = Thread(Runnable {
            try {

                val service = ConversationService(ConversationService.VERSION_DATE_2017_02_03)
                service.setUsernameAndPassword("da8c1602-204f-4985-86ba-322d6cc2b5f5", "f0tERswslz7p")
                val newMessage = MessageRequest.Builder().inputText(inputmessage).context(context).build()
                val response = service.message("39047a9c-a763-4570-9b3e-9fe96574624f", newMessage).execute()

                //Passing Context of last conversation
                if (response!!.context != null) {
                    context.clear()
                    context = response.context

                }
                val outMessage = Message()
                if (response.output != null && response.output.containsKey("text")) {
                    val responseList = response.output["text"] as ArrayList<*>
                    if (responseList.size > 0) {
                        outMessage.message = responseList[0] as String
                        outMessage.id = "2"
                    }
                    messageArrayList!!.add(outMessage)
                }

                runOnUiThread {
                    mAdapter!!.notifyDataSetChanged()
                    if (mAdapter!!.itemCount > 1) {
                        recyclerView!!.layoutManager.smoothScrollToPosition(recyclerView, null, mAdapter!!.itemCount - 1)

                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()

    }

    //Record a message via Watson Speech to Text



    //Private Methods - Speech to Text
    private //.model("en-UK_NarrowbandModel")
    //TODO: Uncomment this to enable Speaker Diarization
    //.speakerLabels(true)
    val recognizeOptions: RecognizeOptions
        get() = RecognizeOptions.Builder()
                .continuous(true)
                .contentType(ContentType.OPUS.toString())
                .interimResults(true)
                .inactivityTimeout(2000)
                .build()


    private fun showMicText(text: String) {
        runOnUiThread { inputMessage!!.setText(text) }
    }


    private fun showError(e: Exception) {
        runOnUiThread {
            e.printStackTrace()
        }
    }

    companion object {
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val RECORD_REQUEST_CODE = 101
    }

}

