package com.hello.hello_matrix_flutter.src.rooms

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import com.hello.hello_matrix_flutter.src.timeline.TimeLineController
import io.flutter.Log
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.matrix.android.sdk.api.MatrixCallback
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams
import org.matrix.android.sdk.api.session.room.model.message.MessageType
import java.io.File
import com.hello.hello_matrix_flutter.HelloMatrixFlutterPluginMethodChannel.Companion.instance as MethodChannelInstance

class RoomController {
    var _tag = "RoomController"
    var timeLineController: TimeLineController? = null
    
    fun createDirectRoom(userId: String?, roomName: String?) {
        val existingRoom = SessionHolder.matrixSession!!.getExistingDirectRoomWithUser(userId!!)
        if (existingRoom != null) {
            Log.i(_tag, "room already exist $existingRoom")
            MethodChannelInstance?.result?.success(existingRoom)
            return
        }
        Log.i(_tag, userId)
        val createRoomParams = CreateRoomParams()
        createRoomParams.name = roomName
        //createRoomParams.enableEncryption();
        createRoomParams.setDirectMessage()
        createRoomParams.invitedUserIds.add(userId)
        createRoomParams.enableEncryptionIfInvitedUsersSupportIt = false
        SessionHolder.matrixSession!!.createRoom(createRoomParams, object : MatrixCallback<String?> {
            override fun onFailure(failure: Throwable) {
                super.onFailure(failure)
                Log.i(_tag, "on failed create room")
                MethodChannelInstance?.result?.error("", "", null)
            }

            override fun onSuccess(roomId: String?) {
                super.onSuccess(roomId)
                Log.i(_tag, "on success create room")
                Log.i(_tag, roomId!!)
                MethodChannelInstance?.result?.success(roomId)
            }
        })
    }

    fun sendSimpleTextMessage(roomId: String?, body: String?) {
        val room = SessionHolder.matrixSession!!.getRoom(roomId!!)
        room!!.sendTextMessage(text=body!!, msgType=MessageType.MSGTYPE_TEXT, autoMarkdown=false)
        if(room==null){
            MethodChannelInstance?.result?.success(false)
        }else{
            MethodChannelInstance?.result?.success(true)
        }

    }

   
    fun sendSingleImageMessage(roomId: String,body: String?, path: String){
        var room: Room? = SessionHolder.matrixSession?.getRoom(roomId)
        SessionHolder.matrixSession?.myUserId?.let { Log.i(_tag, it) }
        room?.roomId?.let { Log.i(_tag, it) }

        var uri: Uri = Uri.parse(path)
        val file : File = File(uri.path)
        val cFile: File = File(file.parent, file.name)
                .copyTo(File(SessionHolder.appContext?.cacheDir, file.name), true)
        val mimiType = getMimeType(uri)
        uri = Uri.fromFile(cFile)
        var attachment = uri?.let {
            ContentAttachmentData(
                    mimeType = mimiType,
                    type = ContentAttachmentData.Type.IMAGE,
                    queryUri = it,
                    name = body
            )
        }
        if (attachment != null) {
            room?.sendMedia(attachment,true,emptySet())
        }

        //result handle
        if(room==null){
            MethodChannelInstance?.result?.success(false)
        }else{
            MethodChannelInstance?.result?.success(true)
        }
    }

    fun sendMultipleImageMessage(roomId: String,jsonData: String){
        var room: Room? = SessionHolder.matrixSession?.getRoom(roomId)
        SessionHolder.matrixSession?.myUserId?.let { Log.i(_tag, it) }
        room?.roomId?.let { Log.i(_tag, it) }

        var jsonArray = JSONArray(jsonData)
        var contentAttachmentData: MutableList<ContentAttachmentData> = mutableListOf()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)

            var body:String = item.getString("body")

            var uri: Uri = Uri.parse(item.getString("path"))
            val file : File = File(uri.path)
            val cFile: File = File(file.parent, file.name)
                    .copyTo(File(SessionHolder.appContext?.cacheDir, file.name), true)
            val mimiType = getMimeType(uri)
            uri = Uri.fromFile(cFile)
            var attachment = uri?.let {
                ContentAttachmentData(
                        mimeType = mimiType,
                        type = ContentAttachmentData.Type.IMAGE,
                        queryUri = it,
                        name = body
                )
            }
            contentAttachmentData.add(attachment!!)
        }


        if (contentAttachmentData != null) {
            room?.sendMedias(contentAttachmentData,true,emptySet())
        }

        //result handle
        if(room==null){
            MethodChannelInstance?.result?.success(false)
        }else{
            MethodChannelInstance?.result?.success(true)
        }
    }

    fun createTimeLine(roomId: String?) {
        Log.i(_tag,"timelineCtrlInited");
        timeLineController = TimeLineController(roomId)
    }


    fun joinRoom(roomId: String?){

        val room = SessionHolder.matrixSession!!.getRoom(roomId!!)
        runBlocking {
            if(room != null){
                room?.join(reason = "")
                MethodChannelInstance?.result?.success(true)
            }else{
                MethodChannelInstance?.result?.success(false)
            }

        }
    }

    /*fun joinRoomPrivateFun(roomId: String?) {
        //val room = SessionHolder.matrixSession!!.getRoom(roomId!!)
        *//*if(room==null){
            MethodChannelInstance?.result?.success(false)
        }else{
            room?.join(reason = "",viaServers = emptyList())
            MethodChannelInstance?.result?.success(true)
        }*//*

    }*/
    fun destroyTimeLine() {
        timeLineController!!.destroyTimeLine()
        timeLineController = null
    }
    private fun getMimeType(uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver? = SessionHolder.appContext?.contentResolver
            cr?.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                    uri
                            .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase()
            )
        }
        return mimeType
    }

}