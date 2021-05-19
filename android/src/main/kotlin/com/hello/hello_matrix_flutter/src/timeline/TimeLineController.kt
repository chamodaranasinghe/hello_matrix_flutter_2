package com.hello.hello_matrix_flutter.src.timeline

import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import com.hello.hello_matrix_flutter.HelloMatrixFlutterPluginMethodChannel
import com.hello.hello_matrix_flutter.PluginBindingHolder
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.session.content.ContentUrlResolver
import org.matrix.android.sdk.api.session.events.model.Content
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings
import java.io.File
import java.util.*
import kotlin.collections.HashMap


class TimeLineController(var roomId: String?) : Timeline.Listener, EventChannel.StreamHandler {
    var _tag = "ChatTimeLine"
    var room: Room? = null
    private var timeLineEventChannel: EventChannel? = null
    var eventSink: EventSink? = null
    var typingUsersStreamHandler: TypingUsersStreamHandler? = null
    var timeline: Timeline? = null
    var timelineSettings = TimelineSettings(
            10,
            true)

    private fun init() {
        Log.i(_tag,"Timeline ctrl initiated")
        room = SessionHolder.matrixSession!!.getRoom(roomId!!)
        timeline = room!!.createTimeline(null, timelineSettings)
        timeline!!.addListener(this)
        timeline!!.isLive
        timeline!!.start()
        timeLineEventChannel = EventChannel(PluginBindingHolder.flutterPluginBinding?.binaryMessenger, "hello_matrix_flutter/timelineEvents")
        timeLineEventChannel!!.setStreamHandler(this)

        //start users typing tracker
        typingUsersStreamHandler = TypingUsersStreamHandler(room!!)
    }

    override fun onNewTimelineEvents(list: List<String>) {
        Log.i(_tag, "onNewTimelineEvents")
    }

    override fun onTimelineFailure(throwable: Throwable) {
        Log.i(_tag, "onTimelineFailure")
        Log.i(_tag, "")
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    override fun onTimelineUpdated(list: List<TimelineEvent>) {
        Log.i(_tag, "onTimelineUpdated")
        if (eventSink != null) {
            val events: MutableList<TimeLineEventLite> = ArrayList()

            list.forEach { event ->
                Log.i(_tag, event.root.type!!)
                val timeLineEventLite = TimeLineEventLite()
                timeLineEventLite.type = event.root.type
                timeLineEventLite.localId = event.localId
                timeLineEventLite.eventId = event.eventId
                timeLineEventLite.originServerTs = event.root.originServerTs!!
                timeLineEventLite.localTs = event.root.ageLocalTs!!
                if (event.root.type == "m.room.message") {
                    /*timeLineEventLite.clearedContent = JSONObject(event.root.getClearContent()).toString()*/
                    timeLineEventLite.clearedContent = event.root.getClearContent()?.let { handleClearedContent(it) }
                }
                if (event.senderInfo.userId == SessionHolder.matrixSession!!.myUserId) {
                    timeLineEventLite.direction = "sent"
                } else {
                    timeLineEventLite.direction = "received"
                }
                event.root.getClearContent()
                Log.i(_tag, "type " + event.root.type)
                //Log.i(_tag,"clear type "+event.getRoot().getType());
                //Log.i(_tag,"clear content "+event.getRoot().getClearContent());
                Log.i(_tag,"timeLineEventLite clear content "+timeLineEventLite.clearedContent);
                events.add(timeLineEventLite)
            }
            val jsonArrayEvents = JSONArray()
            for (timeLineEventLite in events) {
                val j = JSONObject()
                try {
                    j.put("type", timeLineEventLite.type)
                    j.put("localId", timeLineEventLite.localId)
                    j.put("eventId", timeLineEventLite.eventId)
                    j.put("originServerTs", timeLineEventLite.originServerTs)
                    j.put("localTs", timeLineEventLite.localTs)
                    j.put("clearedContent", timeLineEventLite.clearedContent)
                    //Log.i(_tag, "clear content " + timeLineEventLite.clearedContent)
                    j.put("direction", timeLineEventLite.direction)
                    jsonArrayEvents.put(j)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            eventSink!!.success(jsonArrayEvents.toString())
        }
    }

    fun destroyTimeLine() {
        Log.i(_tag, "destroyTimeLine")
        timeline!!.dispose()
        timeline!!.removeAllListeners()
        typingUsersStreamHandler!!.disposeTypingTracker()
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        eventSink = events
        Log.i(_tag, "onListenEvents")
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
        Log.i(_tag, "onCancelEvents")
    }

    //region Supportive methods
    fun paginateBackward() {
        //when this is called, paginate backwards and onTimeLineUpdated is called
        if (timeline!!.hasMoreToLoad(Timeline.Direction.BACKWARDS)) {
            timeline!!.paginate(Timeline.Direction.BACKWARDS, 10)
            HelloMatrixFlutterPluginMethodChannel.instance?.result?.success(true)
        } else {
            HelloMatrixFlutterPluginMethodChannel.instance?.result?.success(false)
        }
    }

    fun onStartTyping() {
        room!!.userIsTyping()
        HelloMatrixFlutterPluginMethodChannel.instance?.result?.success(true)
    }

    fun onStopTyping() {
        room!!.userStopsTyping()
        HelloMatrixFlutterPluginMethodChannel.instance?.result?.success(true)
    } //endregion

    init {
        init()
    }

    private fun handleClearedContent(content: Content): String {
        var decoded = "{}"

        var contentType = content["msgtype"];

        when (contentType) {
            "m.text" -> {
                decoded = JSONObject(content).toString()
            }
            "m.image" -> {
                Log.i(_tag,"img content ${content["url"].toString()}")

                Log.i(_tag,"img content full ${getClearedFullUrl(content["url"].toString())}")
                Log.i(_tag,"img content thumb ${getClearedThumbUrl(content["url"].toString())}")


                var full:String? = null
                var thumb:String?= null
                var local:String?= null

                var isLocal :Boolean = URLUtil.isFileUrl(content["url"].toString())
                if(isLocal){
                    var uri: Uri = Uri.parse(content["url"].toString())
                    local = uri.path
                }else{
                    full = getClearedFullUrl(content["url"].toString())
                    thumb = getClearedThumbUrl(content["url"].toString())
                }

                val map:HashMap<String, String?> = HashMap<String, String?>()
                map["msgtype"] = "m.image"
                map["full"] = full
                map["thumb"] = thumb
                map["local"] = local
                map["body"] = content["body"].toString()
                decoded = JSONObject(map as Map<String, String?>).toString()


            }
            else -> { // Note the block

            }
        }
        return decoded
    }

    private fun getClearedFullUrl(mxUrl: String): String? {
        return SessionHolder.matrixSession?.contentUrlResolver()?.resolveFullSize(mxUrl)
    }

    private fun getClearedThumbUrl(mxUrl: String): String? {
        return SessionHolder.matrixSession?.contentUrlResolver()?.resolveThumbnail(
                contentUrl = mxUrl,
                height = 500,
                width = 500,
                method = ContentUrlResolver.ThumbnailMethod.CROP
        )
    }
}
