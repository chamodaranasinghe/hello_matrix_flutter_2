package com.hello.hello_matrix_flutter.src.timeline

class TimeLineEventLite {
    var type: String? = null
    var eventId: String? = null
    var localId: Long = 0
    var originServerTs: Long = 0
    var localTs: Long = 0
    var clearedContent: String? = null
    var direction: String? = null
}