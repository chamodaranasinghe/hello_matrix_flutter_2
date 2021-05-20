package com.hello.hello_matrix_flutter.src.rooms

import android.util.Log
import androidx.lifecycle.Observer
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import com.hello.hello_matrix_flutter.src.directory.DirectoryConnector
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import org.json.JSONArray
import org.json.JSONObject
import org.matrix.android.sdk.api.session.room.RoomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import java.util.*


class RoomListStreamHandler : EventChannel.StreamHandler {
    var eventSink: EventSink? = null
    val _tag = "RoomListStreamHandler"
    private var observer = Observer<List<RoomSummary>> { roomSummaries ->
        if (eventSink != null) {
            val rooms: MutableList<RoomSummaryLite> = ArrayList()

            roomSummaries.forEach { roomSummary ->
                Log.i(_tag, roomSummary.roomId)
                if (!roomSummary.isDirect) {
                    return@Observer
                }
                val roomSummaryLite = RoomSummaryLite()
                roomSummaryLite.roomId = roomSummary.roomId
                roomSummaryLite.roomName = roomSummary.displayName
                roomSummaryLite.roomTopic = roomSummary.topic
                roomSummaryLite.isDirect = roomSummary.isDirect
                roomSummaryLite.notificationCount = roomSummary.notificationCount
                if (roomSummary.latestPreviewableEvent != null) {
                    roomSummaryLite.originServerLastEventTs = roomSummary.latestPreviewableEvent?.root?.originServerTs!!
                    roomSummaryLite.localLastEventTs = roomSummary.latestPreviewableEvent?.root?.ageLocalTs!!
                } else {
                    roomSummaryLite.originServerLastEventTs = 0
                    roomSummaryLite.localLastEventTs = 0
                }
                roomSummaryLite.membership = roomSummary.membership.toString()
                roomSummaryLite.lastEvent = roomSummary.latestPreviewableEvent
                if (roomSummary.otherMemberIds.isNotEmpty()) {
                    val userProfile = DirectoryConnector.pullUserProfile(roomSummary.otherMemberIds[0])
                    roomSummaryLite.otherUserMatrixId = roomSummary?.otherMemberIds[0]
                    val homeServerHost = SessionHolder.matrixSession!!.sessionParams.homeServerHost
                    roomSummaryLite.otherUserHelloId = roomSummaryLite.otherUserMatrixId.toString().replace(homeServerHost!!, "").replace(":", "").replace("@", "")
                    if (userProfile != null) {
                        roomSummaryLite.otherMemberDisplayName = userProfile.firstName + " " + userProfile.lastName
                        roomSummaryLite.otherMemberThumbnail = userProfile.photoThumbnail
                    } else {
                        roomSummaryLite.otherMemberDisplayName = SessionHolder.matrixSession!!.getUser(roomSummary.otherMemberIds[0])!!.displayName
                        roomSummaryLite.otherMemberThumbnail = null
                    }
                } else {
                    roomSummaryLite.otherMemberDisplayName = ""
                    roomSummaryLite.otherMemberThumbnail = null
                    roomSummaryLite.otherUserHelloId = null
                    roomSummaryLite.otherUserMatrixId = null
                }
                rooms.add(roomSummaryLite)
            }
            Collections.sort(rooms, Collections.reverseOrder<Any>())
            val jsonArrayRooms = JSONArray()
            for (roomLite in rooms) {
                val j = JSONObject()
                try {
                    j.put("roomId", roomLite.roomId)
                    j.put("roomName", roomLite.roomName)
                    j.put("roomTopic", roomLite.roomTopic)
                    j.put("isDirect", roomLite.isDirect)
                    j.put("notificationCount", roomLite.notificationCount)
                    j.put("avatarUrl", roomLite.avatarUrl)
                    j.put("originServerLastEventTs", roomLite.originServerLastEventTs)
                    j.put("localLastEventTs", roomLite.localLastEventTs)
                    j.put("membership", roomLite.membership)
                    j.put("isEncrypted", SessionHolder.matrixSession!!.getRoom(roomLite.roomId!!)!!.isEncrypted())
                    j.put("encryptionAlgorithm", SessionHolder.matrixSession!!.getRoom(roomLite.roomId!!)!!.encryptionAlgorithm())
                    j.put("shouldEncryptForInvitedMembers", SessionHolder.matrixSession!!.getRoom(roomLite.roomId!!)!!.shouldEncryptForInvitedMembers())
                    if (roomLite.lastEvent != null) {
                        if (roomLite.lastEvent!!.root.getClearType() == "m.room.message") {
                            j.put("lastContent", JSONObject(roomLite.lastEvent!!.root.getClearContent()).toString())
                        }
                    }
                    j.put("otherMemberDisplayName", roomLite.otherMemberDisplayName)
                    j.put("otherMemberThumbnail", roomLite.otherMemberThumbnail)
                    j.put("otherUserHelloId", roomLite.otherUserHelloId)
                    jsonArrayRooms.put(j)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            eventSink!!.success(jsonArrayRooms.toString())
        }
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        Log.i(_tag, "onListenEvents")
        if (SessionHolder.matrixSession == null) {
            return
        }
        eventSink = events
        SessionHolder.matrixSession!!.getRoomSummariesLive(RoomSummaryQueryParams.Builder().build()).observeForever(observer)
    }

    override fun onCancel(arguments: Any?) {
        if (SessionHolder.matrixSession == null) {
            return
        }
        SessionHolder.matrixSession!!.getRoomSummariesLive(RoomSummaryQueryParams.Builder().build()).removeObserver(observer)
        eventSink = null
    }
}
