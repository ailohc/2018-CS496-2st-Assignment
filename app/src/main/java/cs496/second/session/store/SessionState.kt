package cs496.second.session.store

import cs496.second.session.model.LoginProvider
import cs496.second.session.model.User
import cs496.second.util.Task

data class SessionState(val verified: Boolean = false,
                        val createAccountTask: Task = Task(),
                        val loginTask: Task = Task(),
                        val verifyUserTask: Task = Task(),
                        val verificationEmailTask: Task = Task(),
                        val refreshUserTask: Task = Task(),
                        val providers: List<LoginProvider> = listOf(),
                        val loggedUser: User? = null)