package cs496.second.profile.store

import cs496.second.profile.model.PrivateData
import cs496.second.profile.model.PublicProfile
import cs496.second.util.Task
import mini.Action

data class LoadUserDataCompleteAction(val privateData: PrivateData?,
                                      val publicProfile: PublicProfile?,
                                      val task: Task) : Action