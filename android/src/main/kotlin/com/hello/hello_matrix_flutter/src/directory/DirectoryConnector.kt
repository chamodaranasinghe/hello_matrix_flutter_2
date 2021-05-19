package com.hello.hello_matrix_flutter.src.directory

import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import org.apache.commons.collections4.IterableUtils

class DirectoryConnector {
    var _tag = "DirectoryConnector"
  /*  fun pullUserProfile(mxUserId: String): UserProfile? {
        val homeServerHost = SessionHolder.matrixSession!!.sessionParams.homeServerHost
        val helloId = mxUserId.replace(homeServerHost!!, "").replace(":", "").replace("@", "")
        val directory: MutableList<UserProfile>? = DirectoryInstance.get()
        return if (directory!!.isEmpty()) {
            null
        } else {
            //val userProfile = IterableUtils.find()
            null
        }
    }*/

    companion object {
        fun pullUserProfile(mxUserId: String): UserProfile? {
            val homeServerHost = SessionHolder.matrixSession!!.sessionParams.homeServerHost
            val helloId = mxUserId.replace(homeServerHost!!, "").replace(":", "").replace("@", "")
            val directory: MutableList<UserProfile>? = DirectoryInstance.get()
            return if (directory!!.isEmpty()) {
                null
            } else {
                return IterableUtils.find(directory) { `object` -> `object`.helloId == helloId }
            }
        }
    }
}
