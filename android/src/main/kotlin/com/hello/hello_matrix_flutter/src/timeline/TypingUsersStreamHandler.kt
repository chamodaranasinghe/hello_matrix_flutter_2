package com.hello.hello_matrix_flutter.src.timeline

import android.util.Log
import androidx.lifecycle.Observer
import com.hello.hello_matrix_flutter.PluginBindingHolder
import com.hello.hello_matrix_flutter.src.directory.DirectoryConnector
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.util.Optional
import java.util.*


class TypingUsersStreamHandler(var room: Room) : EventChannel.StreamHandler {
    var _tag = "TypingUsersStreamHandler"
    private lateinit var typingUserStreamHandlerChannel: EventChannel
    var eventSink: EventSink? = null
    private var observer = Observer<Optional<RoomSummary>> { roomSummaryOptional ->
        val summary = roomSummaryOptional.getOrNull()
        if (summary != null) {
            Log.i(_tag, "not null")
            Log.i(_tag, summary.roomId)
            val typingUserNames: MutableList<String?> = ArrayList()
            if (summary.typingUsers.isEmpty()) {
                typingUserNames.clear()
                if (eventSink != null) {
                    eventSink!!.success(typingUserNames)
                    return@Observer
                }
                return@Observer
            }
            Log.i(_tag,"count"+summary.typingUsers.size)
            summary.typingUsers.forEach { user ->
                val profile = DirectoryConnector.pullUserProfile(user.userId)
                if (profile != null) {
                    val fullName = profile!!.firstName + " " + profile!!.lastName
                    typingUserNames.add(fullName)
                    if (eventSink != null) {
                        eventSink!!.success(typingUserNames)
                        return@Observer
                    }
                } else {
                    typingUserNames.add(user.displayName)
                }
            }
        } else {
            Log.i(_tag, "null")
            val typingUserNames: List<String> = ArrayList()
            if (eventSink != null) {
                eventSink!!.success(typingUserNames)
                return@Observer
            }
            return@Observer
        }
    }


    private fun init() {
        Log.i(_tag,"inited")
        typingUserStreamHandlerChannel = EventChannel(PluginBindingHolder.flutterPluginBinding?.binaryMessenger, "hello_matrix_flutter/typingUsersEvents")
        typingUserStreamHandlerChannel!!.setStreamHandler(this)
    }

    fun disposeTypingTracker() {
        room!!.getRoomSummaryLive().removeObserver(observer)
        typingUserStreamHandlerChannel!!.setStreamHandler(null)
    }
    init {
        init()
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        eventSink = events!!
        room!!.getRoomSummaryLive().observeForever(observer)
    }

    override fun onCancel(arguments: Any?) {
        room!!.getRoomSummaryLive().removeObserver(observer)
        eventSink = null
    }
}
