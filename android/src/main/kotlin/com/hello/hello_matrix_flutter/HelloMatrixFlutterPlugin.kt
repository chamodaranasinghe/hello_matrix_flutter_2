package com.hello.hello_matrix_flutter

import android.util.Log
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import com.hello.hello_matrix_flutter.src.auth.SessionHolder.matrixInstance
import com.hello.hello_matrix_flutter.src.directory.DirectoryController
import com.hello.hello_matrix_flutter.src.rooms.RoomListStreamHandler
import com.hello.hello_matrix_flutter.src.users.UserListStreamHandler
import com.hello.hello_matrix_flutter.src.util.RoomDisplayNameFallbackProviderImpl
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.Matrix.Companion.initialize
import org.matrix.android.sdk.api.MatrixConfiguration
import java.util.*

/**
 * HelloMatrixFlutterPlugin
 */
class HelloMatrixFlutterPlugin : FlutterPlugin {
    private var methodChannel: MethodChannel? = null
    private var roomListEventChannel: EventChannel? = null
    private var userListEventChannel: EventChannel? = null
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {        
        Log.i("on attached", "on attached")
        //set plugin binding for the entire plugin
        PluginBindingHolder.flutterPluginBinding = flutterPluginBinding
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "hello_matrix_flutter")
        methodChannel!!.setMethodCallHandler(HelloMatrixFlutterPluginMethodChannel.instance)
        Log.i("method channel created","method channel created")
        roomListEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "hello_matrix_flutter/roomListEvents")
        roomListEventChannel!!.setStreamHandler(RoomListStreamHandler())
        userListEventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "hello_matrix_flutter/userListEvents")
        userListEventChannel!!.setStreamHandler(UserListStreamHandler())

        SessionHolder.appContext = flutterPluginBinding.applicationContext


        //init Matrix
        initialize(
                context = flutterPluginBinding.applicationContext,
                matrixConfiguration = MatrixConfiguration(
                        roomDisplayNameFallbackProvider = RoomDisplayNameFallbackProviderImpl()
                )
        )
        // It returns a singleton
        val matrix = Matrix.getInstance(flutterPluginBinding.applicationContext)
        // You can then grab the authentication service and search for a known session
        val lastSession = matrix.authenticationService().getLastAuthenticatedSession()
        if (lastSession != null) {
            SessionHolder.matrixSession = lastSession
            // Don't forget to open the session and start syncing.
            lastSession.open()
            lastSession.startSync(true)
            DirectoryController().retrieveDirectory()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        PluginBindingHolder.flutterPluginBinding = null
        methodChannel!!.setMethodCallHandler(null)
        roomListEventChannel!!.setStreamHandler(null)
        userListEventChannel!!.setStreamHandler(null)
    }
}