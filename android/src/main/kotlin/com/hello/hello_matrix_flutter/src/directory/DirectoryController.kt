package com.hello.hello_matrix_flutter.src.directory

import android.util.Log
import androidx.room.Room
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import com.hello.hello_matrix_flutter.src.storage.DataStorage
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await
import java.util.*
import com.hello.hello_matrix_flutter.HelloMatrixFlutterPluginMethodChannel.Companion.instance as MethodChannelInstance

class DirectoryController {
  var _tag = "DirectoryController"

    suspend fun updateDirectory() {
        val dataStorage = DataStorage()
        var orgCode = ""
        var userCode = ""
        val profileData = dataStorage.getStringData(DataStorage.KEY_PROFILE_STORAGE)
        try {
            val profileDataJson = JSONObject(profileData)
            orgCode = profileDataJson.getString("org_prefix")
            userCode = profileDataJson.getString("hello_id")
        } catch (e: JSONException) {
            e.printStackTrace()
            MethodChannelInstance!!.result!!.error("",e.message,e.message)
            return
        }
        //setting the request body
        val endPoint = "${DirectoryClient.getEndpoint(endpoint = "global_contacts")}?org_prefix=$orgCode&hello_id=$userCode"
        var response: Response? = null;
        try {
            response = DirectoryClient.getRequest(endPoint)?.let { DirectoryClient.okHttpClient.newCall(it).await() }
        } catch (e: Exception) {
            Log.i(_tag, e.toString())
            MethodChannelInstance!!.result!!.error("",e.message,e.message)
            return
        }
        if (response?.isSuccessful != true) {
            MethodChannelInstance!!.result!!.error("","http response error","http response error")
            return
        }
        //continue with result
        try {
            val directoryData = response.body!!.string()
            saveNewDataInLocalDbAndUpdateInstance(json = directoryData)
            MethodChannelInstance!!.result!!.success(true)
            return
        } catch (e: Exception) {
            MethodChannelInstance!!.result!!.error("",e.message,e.message)
            Log.i(_tag, e.toString())
            return
        }
    }

    fun retrieveDirectory() {
        if (DirectoryInstance.get()?.isEmpty() == true) {
            refreshDirectory()
        }
        val list: MutableList<UserProfile>? = DirectoryInstance.get()
        val jsonArray = JSONArray()
        for (p in list!!) {
            val j = JSONObject()
            try {
                j.put("hello_id", p.helloId)
                j.put("first_name", p.firstName)
                j.put("last_name", p.lastName)
                j.put("email", p.email)
                j.put("contact", p.contact)
                j.put("job_title", p.jobTitle)
                j.put("photo", p.photoUrl)
                j.put("thumbnail", p.photoThumbnail)
                j.put("org_prefix", p.orgPrefix)
                j.put("org_name", p.orgName)
                j.put("org_contact", p.orgContact)
                j.put("org_website", p.orgWebsite)
                jsonArray.put(j)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        MethodChannelInstance!!.result!!.success(jsonArray.toString())
        ///TODO add to stream every time when directory is refreshed
    }

    //pull from local db and update current list
    private fun refreshDirectory() {
        val userProfileDao = DatabaseClient.getInstance().appDatabase.userProfileDao()
        DirectoryInstance.set(userProfileDao!!.getAll()!!.toMutableList())
        ///TODO add to stream every time when directory is refreshed
    }

    fun eraseDirectory() {
        val userProfileDao = DatabaseClient.getInstance().appDatabase.userProfileDao()
        userProfileDao!!.deleteAll()
        refreshDirectory()
    }

    fun saveNewDataInLocalDbAndUpdateInstance(json: String?): Boolean {
        try {
            val userProfileList: MutableList<UserProfile> = ArrayList()
            val jsonArrayProfiles = JSONArray(json)
            for (i in 0 until jsonArrayProfiles.length()) {
                val j = jsonArrayProfiles.getJSONObject(i)
                val userProfile = UserProfile()
                userProfile.helloId = j.getString("hello_id")
                userProfile.firstName = j.getString("first_name")
                userProfile.lastName = j.getString("last_name")
                userProfile.email = j.getString("email")
                userProfile.contact = j.getString("contact")
                userProfile.jobTitle = j.getString("job_title")
                userProfile.photoUrl = j.getString("photo")
                userProfile.photoThumbnail = j.getString("thumbnail")
                userProfile.orgPrefix = j.getString("org_prefix")
                userProfile.orgName = j.getString("org_name")
                userProfile.orgContact = j.getString("org_contact")
                userProfile.orgWebsite = j.getString("org_website")
                userProfileList.add(userProfile)
            }

            //get directory dao
            val userProfileDao = DatabaseClient.getInstance().appDatabase.userProfileDao()
            userProfileDao!!.deleteAll()
            userProfileDao.insertAll(userProfileList)
            val directory = userProfileDao.getAll()?.toMutableList()
            DirectoryInstance.set(directory)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }
}