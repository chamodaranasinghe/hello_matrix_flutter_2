package com.hello.hello_matrix_flutter.src.timeline

import android.util.Log
import io.flutter.plugin.common.EventChannel

object  TimeLineStreamHandler: EventChannel.StreamHandler  {
    var _tag = "TimeLineStreamHandler"

    init {
        Log.i(_tag,"Singleton invoked")
    }
    lateinit var eventSink: EventChannel.EventSink
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.i(_tag, "onListenEvents")
        if (events != null) {
            eventSink = events
        }
    }

    override fun onCancel(arguments: Any?) {
        Log.i(_tag, "onCancelEvents")
        //eventSink = null
    }
}