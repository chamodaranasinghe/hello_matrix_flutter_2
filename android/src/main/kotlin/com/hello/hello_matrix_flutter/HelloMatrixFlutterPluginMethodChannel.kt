package com.hello.hello_matrix_flutter

import android.os.StrictMode
import android.util.Log
import com.hello.hello_matrix_flutter.src.auth.LoginController
import com.hello.hello_matrix_flutter.src.call.CallController
import com.hello.hello_matrix_flutter.src.directory.DirectoryController
import com.hello.hello_matrix_flutter.src.rooms.RoomController
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlinx.coroutines.runBlocking

class HelloMatrixFlutterPluginMethodChannel private constructor() : MethodCallHandler {
    var result: MethodChannel.Result? = null
    var loginController: LoginController = LoginController()
    var roomController: RoomController = RoomController()
    var directoryController: DirectoryController = DirectoryController()
    var callController:CallController = CallController()
    ///TODO arrange methods in a proper way
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.i("MC", "Method called")
        this.result = result
        when (call.method) {
            //auth
            "checkSession" -> loginController.checkSession(result)
            "login" -> loginController.login(call.argument<Any>("homeServer").toString(), call.argument<Any>("username").toString(), call.argument<Any>("password").toString())
            "getProfile" -> loginController.getProfile()
            "logout" -> loginController.logout()

            //room
            "sendSingleImageMessage"->roomController.sendSingleImageMessage(call.argument<Any>("roomId").toString(),call.argument<Any>("body").toString(),call.argument<Any>("path").toString())
            "sendMultipleImageMessage"->roomController.sendMultipleImageMessage(call.argument<Any>("roomId").toString(),call.argument<Any>("jsonData").toString())
            "createDirectRoom" -> roomController.createDirectRoom(call.argument<Any>("userId").toString(), call.argument<Any>("roomName").toString())
            "sendSimpleTextMessage" -> roomController.sendSimpleTextMessage(call.argument<Any>("roomId").toString(), call.argument<Any>("body").toString())
            "createTimeLine" -> roomController.createTimeLine(call.argument<Any>("roomId").toString())
            "destroyTimeLine" -> roomController.destroyTimeLine()
            "joinRoom" -> roomController.joinRoom(call.argument<Any>("roomId").toString())
            "paginateBackward" -> roomController.timeLineController!!.paginateBackward()
            "onStartTyping" -> roomController.timeLineController!!.onStartTyping()
            "onStopTyping" -> roomController.timeLineController!!.onStopTyping()

            //directory
            "updateDirectory" -> {
                val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy);
                runBlocking { directoryController.updateDirectory() }

            }
            "retrieveDirectory" -> directoryController.retrieveDirectory()
            //calling
            "getTurnServerCredentials" -> callController.getTurnServerCredentials()
            else -> {
                result.notImplemented()
                return
            }
        }
    }

    companion object {
        private var INSTANCE: HelloMatrixFlutterPluginMethodChannel? = null
        val instance: HelloMatrixFlutterPluginMethodChannel?
            get() {
                if (INSTANCE == null) {
                    Log.i("MCCR", "MCCR")
                    INSTANCE = HelloMatrixFlutterPluginMethodChannel()
                }
                return INSTANCE
            }
    }

}