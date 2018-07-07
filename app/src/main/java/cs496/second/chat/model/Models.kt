package cs496.second.chat.model

import com.google.firebase.firestore.ServerTimestamp
import cs496.second.profile.model.UserData
import java.util.*

data class Message(val uid: String = "",
                   val author: UserData,
                   val message: String,
                   @ServerTimestamp val timestamp: Date)