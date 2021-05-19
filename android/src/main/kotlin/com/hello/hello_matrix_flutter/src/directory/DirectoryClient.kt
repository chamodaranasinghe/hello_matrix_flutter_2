package com.hello.hello_matrix_flutter.src.directory

import okhttp3.OkHttpClient
import okhttp3.Request

object DirectoryClient {
    private val TAG = "DirectoryClient"
    private val BASE_URL = "https://admin.hellodesk.app/_client/"
    private val TOKEN = "P8qTgBpTKAwFpzhBlxw7AA=="

    init {

    }
    val okHttpClient: OkHttpClient = OkHttpClient()
    fun getEndpoint(endpoint: String): String {
        return BASE_URL + endpoint
    }
    fun getRequest(endPoint: String?): Request? {
        return endPoint?.let {
            Request.Builder()
                .url(it)
                .header("Basic-Token", TOKEN)
                .build()
        }
    }



}