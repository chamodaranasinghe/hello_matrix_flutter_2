package com.hello.hello_matrix_flutter.src.users

import android.util.Log
import androidx.lifecycle.Observer
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import com.hello.hello_matrix_flutter.src.directory.DirectoryConnector
import com.hello.hello_matrix_flutter.src.directory.UserProfile
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.session.user.model.User


class UserListStreamHandler : EventChannel.StreamHandler {
    var eventSink: EventSink? = null
    private var observer = Observer<List<User>> { users ->
        if (eventSink != null) {
            val jsonArrayUsers = JSONArray()
            
            users.forEach { user ->
                val j = JSONObject()
                try {
                    if (user.userId == SessionHolder.matrixSession!!.myUserId) {
                    } else {
                        var p:UserProfile? = DirectoryConnector.pullUserProfile(user.userId)
                        if (p != null) {
                            j.put("hello_id", p.helloId)
                            j.put("first_name", p.firstName)
                            j.put("last_name", p.lastName)
                            j.put("email", p.email)
                            j.put("contact", p.contact)
                            j.put("job_title", p.jobTitle)
                            j.put("photo", p.photoUrl)
                            j.put("thumbnail", p.photoThumbnail)
                            j.put("org_prefix", p.orgPrefix)
                            j.put("org_name", p.orgName)
                            j.put("org_contact", p.contact)
                            j.put("org_website", p.orgWebsite)
                            j.put("mxUserId", user.userId)
                            jsonArrayUsers.put(j)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            eventSink!!.success(jsonArrayUsers.toString())
        }
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        if (SessionHolder.matrixSession == null) {
            return
        }
        eventSink = events
        SessionHolder.matrixSession!!.getUsersLive().observeForever(observer)
    }

    override fun onCancel(arguments: Any?) {
        if (SessionHolder.matrixSession == null) {
            return
        }
        SessionHolder.matrixSession!!.getUsersLive().removeObserver(observer)
        eventSink = null
    }
}
