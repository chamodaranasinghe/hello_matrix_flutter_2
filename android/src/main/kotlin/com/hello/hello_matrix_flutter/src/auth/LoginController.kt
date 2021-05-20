package com.hello.hello_matrix_flutter.src.auth

import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.hello.hello_matrix_flutter.src.auth.SessionHolder
import com.hello.hello_matrix_flutter.src.directory.DirectoryClient
import com.hello.hello_matrix_flutter.src.directory.DirectoryController
import com.hello.hello_matrix_flutter.src.storage.DataStorage
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.auth.login.LoginWizard
import org.matrix.android.sdk.api.session.Session
import ru.gildor.coroutines.okhttp.await
import com.hello.hello_matrix_flutter.HelloMatrixFlutterPluginMethodChannel.Companion.instance as MethodChannelInstance

class LoginController {
   val _tag = "LoginController"
    var directoryController = DirectoryController()
    fun checkSession(result: MethodChannel.Result) {
        val lastSession = SessionHolder.matrixSession
        if (lastSession == null) {
            result.success(false)
            return
        } else if (!lastSession.isOpenable) {
            result.success(false)
            return
        } else {
            result.success(true)
            return
        }
    }

    //main login logic
    fun login(homeServer: String, username: String, password: String) = runBlocking<Unit> {

        var ctx = AppSession.applicationContext

        if(ctx==null){
            Log.i("nuuu","ctx null")
        }

        //allow network in main thread
        val policy: StrictMode.ThreadPolicy  = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy);

        val profileData: String? = getProfileData(username)
        //Log.i(_tag,profileData!!)
        if (profileData == null) {
            MethodChannelInstance?.result?.success(false)
            return@runBlocking
        }

        Log.i(_tag,"profile data received")

        //continue
        val syncDirResult: Boolean = syncDirectoryForFirstTime(profileData)
        //if directory sync fails, return false
        if (!syncDirResult) {
            MethodChannelInstance?.result?.success(false)
            return@runBlocking
        }

        Log.i(_tag,"directory sync complete")

        //continue mx Login
        val mxLoginResult: Boolean = mxLogin(homeServer = homeServer, userName = username, password = password)
        if (!mxLoginResult) {
            MethodChannelInstance?.result?.success(false)
            return@runBlocking
        }

        Log.i(_tag,"mx login complete")

        //finally set user display name
        try {
            val profileDataJson = JSONObject(profileData)
            val fName = profileDataJson.getString("first_name")
            val lName = profileDataJson.getString("last_name")
            val fullName = "$fName $lName"
            val nameSetResult: Boolean = setDisplayName(fullName)
            if (!nameSetResult) {
                MethodChannelInstance?.result?.success(false)
                _logout()
                return@runBlocking
            } else {
                //all success
                MethodChannelInstance?.result?.success(true)
            }

        } catch (e: Exception) {
            MethodChannelInstance?.result?.success(false)
            _logout()
            return@runBlocking
        }
        //save profile data in the shared storage
        val dataStorage = DataStorage()
        dataStorage.storeStringData(key = DataStorage.KEY_PROFILE_STORAGE,data = profileData)
    }

    fun logout() = runBlocking<Unit> {
        val logoutResult: Boolean = _logout()
        if (!logoutResult) {
            MethodChannelInstance?.result?.success(false)
            return@runBlocking
        } else {
            MethodChannelInstance?.result?.success(true)
            return@runBlocking
        }
    }

    //step 1
    private suspend fun getProfileData(userName: String): String? {
        //step 1->get directory and client
        val endPoint = "${DirectoryClient.getEndpoint(endpoint = "profile")}?email=$userName"
        var response: Response? = null;
        try {
            response = DirectoryClient.getRequest(endPoint)?.let { DirectoryClient.okHttpClient.newCall(it).await() }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(_tag, e.toString())
            return null
        }
        if (response?.isSuccessful != true) {
            return null
        }
        //continue with result
        var profileData: String?
        return try {
            profileData = response.body!!.string()
            profileData
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(_tag, e.toString())
            null
        }
    }

    //step 2
    private suspend fun syncDirectoryForFirstTime(profileData: String?): Boolean {
        var orgCode: String
        var userCode: String
        try {
            val profileDataJson = JSONObject(profileData)
            orgCode = profileDataJson.getString("org_prefix")
            userCode = profileDataJson.getString("hello_id")
        } catch (e: JSONException) {
            e.printStackTrace()
            return false;
        }

        val endPoint = "${DirectoryClient.getEndpoint(endpoint = "global_contacts")}?org_prefix=$orgCode&hello_id=$userCode"
        var response: Response? = null;
        try {
            response = DirectoryClient.getRequest(endPoint)?.let { DirectoryClient.okHttpClient.newCall(it).await() }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(_tag, e.toString())
            return false
        }
        if (response?.isSuccessful != true) {
            return false
        }
        //continue with result
        return try {
            val directoryData = response.body!!.string()
            //Log.i(_tag,directoryData)
            directoryController.saveNewDataInLocalDbAndUpdateInstance(json = directoryData)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(_tag, e.toString())
            false
        }
    }

    //step 3
    private suspend fun mxLogin(homeServer: String, userName: String, password: String): Boolean {
        val homeServerConnectionConfig = try {
            HomeServerConnectionConfig
                    .Builder()
                    .withHomeServerUri(Uri.parse(homeServer))
                    .build()
        } catch (failure: Throwable) {
            return false
        }
        SessionHolder.matrixInstance.authenticationService().getLoginFlow(homeServerConnectionConfig)
        //continue login
        val loginWizard: LoginWizard? = SessionHolder.appContext?.let { Matrix.getInstance(it).authenticationService().getLoginWizard() }
        val session: Session = loginWizard?.login(login = userName, password = password, deviceName = (Build.MANUFACTURER + Build.MODEL))
                ?: return false
        SessionHolder.matrixSession = session
        session?.open()
        session?.startSync(true)
        return true
    }

    //step 4
    private suspend fun setDisplayName(displayName: String): Boolean {
        val userId = SessionHolder.matrixSession?.myUserId
        val r = SessionHolder.matrixSession?.setDisplayName(userId = userId!!, newDisplayName = displayName)
        return if (r == null) {
            logout();
            false
        } else {
            true
        }
    }

    private suspend fun _logout(): Boolean {
        if (SessionHolder.matrixSession == null) {
            Log.i(_tag, "Session is null")
            return false
        }
        return try {
            SessionHolder.matrixSession!!.signOut(signOutFromHomeserver = true)
            val dataStorage = DataStorage()
            dataStorage.eraseAllData()
            DirectoryController().eraseDirectory()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getProfile() {
        val dataStorage = DataStorage()
        val profileData = dataStorage.getStringData(DataStorage.KEY_PROFILE_STORAGE)
        if (profileData != null) {
            MethodChannelInstance?.result?.success(profileData)
        } else {
            MethodChannelInstance?.result?.error("-1", "Profile data not found", null)
        }
    }
}