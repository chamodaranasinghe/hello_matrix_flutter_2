package com.hello.hello_matrix_flutter.src.call

import com.hello.hello_matrix_flutter.HelloMatrixFlutterPluginMethodChannel
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.session.call.TurnServerResponse

class CallController {
    var _tag = "CallController"

    fun getTurnServerCredentials() {
        val tsCredentials = JSONObject()
        val tsUris = JSONArray()
        runBlocking {
            val tsResponse:TurnServerResponse =  SessionHolder.matrixSession!!.callSignalingService().getTurnServer()
            tsUris.put(tsResponse.uris!![0])
            tsUris.put(tsResponse.uris!![1])
            tsCredentials.put("uris", tsUris)
            tsCredentials.put("username", tsResponse.username)
            tsCredentials.put("password", tsResponse.password)
            HelloMatrixFlutterPluginMethodChannel.instance?.result?.success(tsCredentials.toString())
        }
    }

}