package cs496.second.profile.store

import cs496.second.profile.model.PrivateData
import cs496.second.profile.model.PublicProfile
import cs496.second.util.Task

data class ProfileState(val privateData: PrivateData? = null,
                        val publicProfile: PublicProfile? = null,
                        val loadProfileTask: Task = Task())