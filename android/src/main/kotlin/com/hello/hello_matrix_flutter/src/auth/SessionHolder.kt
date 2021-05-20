package com.hello.hello_matrix_flutter.src.auth

import android.content.Context
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.Matrix.Companion.getInstance
import org.matrix.android.sdk.api.session.Session

object SessionHolder {
    var appContext: Context? = AppSession.applicationContext
    var matrixSession: Session? = null
    val matrixInstance: Matrix
        get() = getInstance(AppSession.applicationContext)
}