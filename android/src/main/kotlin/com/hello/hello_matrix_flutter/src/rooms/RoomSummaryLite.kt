package com.hello.hello_matrix_flutter.src.rooms

import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent

class RoomSummaryLite : Comparable<RoomSummaryLite> {
    var roomId: String? = null
    var roomName: String? = null
    var roomTopic: String? = null
    var avatarUrl: String? = null
    var membership: String? = null
    var isDirect = false
    var notificationCount = 0
    var originServerLastEventTs: Long = 0
    var localLastEventTs: Long = 0
    var lastEvent: TimelineEvent? = null
    var otherMemberDisplayName: String? = null
    var otherMemberThumbnail: String? = null
    var otherUserHelloId: String? = null
    var otherUserMatrixId: String? = null
    override fun compareTo(old: RoomSummaryLite): Int {
        return java.lang.Long.compare(originServerLastEventTs, old.originServerLastEventTs)
    }
}