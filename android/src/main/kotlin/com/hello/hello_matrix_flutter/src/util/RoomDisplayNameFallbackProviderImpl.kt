package com.hello.hello_matrix_flutter.src.util

import org.matrix.android.sdk.api.RoomDisplayNameFallbackProvider

class RoomDisplayNameFallbackProviderImpl : RoomDisplayNameFallbackProvider {
    override fun getNameFor1member(s: String): String {
        return s
    }

    override fun getNameFor2members(s: String, s1: String): String {
        return "$s and $s1"
    }

    override fun getNameFor3members(s: String, s1: String, s2: String): String {
        return "$s, $s1, and $s2"
    }

    override fun getNameFor4members(s: String, s1: String, s2: String, s3: String): String {
        return "$s, $s1,  $s2 and $s3"
    }

    override fun getNameFor4membersAndMore(s: String, s1: String, s2: String, i: Int): String {
        return "$s, $s1,  $s2 and $i others"
    }

    override fun getNameForEmptyRoom(b: Boolean, list: List<String>): String {
        return "Empty room"
    }

    override fun getNameForRoomInvite(): String {
        return "Room invite"
    }
}