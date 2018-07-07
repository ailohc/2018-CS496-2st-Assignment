package cs496.second.profile.controller

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import cs496.second.core.dagger.AppScope
import cs496.second.core.firebase.*
import cs496.second.core.firebase.FirebaseConstants.LAST_LOGIN
import cs496.second.core.flux.doAsync
import cs496.second.profile.model.PrivateData
import cs496.second.profile.model.PublicProfile
import cs496.second.profile.model.UserData
import cs496.second.profile.store.LoadUserDataCompleteAction
import cs496.second.session.model.User
import cs496.second.util.taskFailure
import cs496.second.util.taskSuccess
import mini.Dispatcher
import javax.inject.Inject

interface ProfileController {
    fun loadUserProfile(user: User)
}

@AppScope
class ProfileControllerImpl @Inject constructor(private val firestore: FirebaseFirestore, val dispatcher: Dispatcher) : ProfileController {
    override fun loadUserProfile(user: User) {
        doAsync {
            try {
                val privateData = getAndCreateIfNoyExistsPrivateData(user)
                val publicProfile = getAndCreateIfNotExistsPublicData(user)
                updateUserLastLogin(userId = user.uid)
                dispatcher.dispatchOnUi(LoadUserDataCompleteAction(privateData, publicProfile, taskSuccess()))
            } catch (e: Throwable) {
                dispatcher.dispatchOnUi(LoadUserDataCompleteAction(null, null, taskFailure(e)))
            }
        }
    }

    private fun updateUserLastLogin(userId: String) {
        Tasks.await(firestore.publicProfileDoc(userId).update(LAST_LOGIN, Timestamp.now()))
    }

    private fun getAndCreateIfNoyExistsPrivateData(user: User): PrivateData {
        val privateDataDocument = Tasks.await(firestore.privateDataDoc(user.uid).get())
        return if (privateDataDocument.exists()) privateDataDocument.toPrivateData()
        else {
            val firebasePrivateData = FirebasePrivateData(user.email)
            Tasks.await(firestore.privateDataDoc(user.uid).set(firebasePrivateData))
            firebasePrivateData.toPrivateData(user.uid)
        }
    }

    private fun getAndCreateIfNotExistsPublicData(user: User): PublicProfile {
        val userData = UserData(user.username, user.photoUrl, user.uid)
        val publicProfile = Tasks.await(firestore.publicProfileDoc(userData.uid).get())
        return if (publicProfile.exists()) publicProfile.toPublicProfile()
        else {
            val firebasePublicProfile = FirebasePublicProfile(userData.toFirebaseUserData(), userData.username.toLowerCase())
            Tasks.await(firestore.publicProfileDoc(userData.uid).set(firebasePublicProfile))
            firebasePublicProfile.toPublicProfile()
        }
    }
}