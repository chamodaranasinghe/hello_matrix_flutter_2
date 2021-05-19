package com.hello.hello_matrix_flutter.src.storage

import android.content.Context
import android.content.SharedPreferences
import com.hello.hello_matrix_flutter.src.auth.SessionHolder

class DataStorage {
    var sharedPref: SharedPreferences
    fun storeStringData(key: String?, data: String?) {
        val editor = sharedPref.edit()
        editor.putString(key, data)
        editor.apply()
    }

    fun getStringData(key: String?): String? {
        return sharedPref.getString(key, null)
    }

    fun eraseAllData() {
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val SP_NAME = "hello"
        const val KEY_PROFILE_STORAGE = "profile"
    }

    init {
        sharedPref = SessionHolder.appContext!!.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }
}