package com.hello.hello_matrix_flutter.src.directory

import android.util.Log
import java.util.*

object DirectoryInstance {

    var _tag = "DirectoryInstance"

    private var directoryList:MutableList<UserProfile>? = mutableListOf<UserProfile>();


    // other instance methods can follow
    fun get(): MutableList<UserProfile>? {
        if (directoryList!!.isEmpty()) {
            //val userProfileDao = DirectoryController().initDB().userProfileDao()
            //directoryList = userProfileDao?.getAll()?.toMutableList()
        }
        return directoryList
    }

    fun set(directoryList: MutableList<UserProfile>?) {
        this.directoryList = directoryList
    }

}



