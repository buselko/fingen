package com.yoshione.fingen.login.googleAuth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.ActivityMenuPro
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class GoogleAuthResponse: InterfaceNewResponse {

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        Log.i("Google Auth", "Google Auth Success")
        val intent = Intent(context, ActivityMenuPro::class.java)
        context.startActivity(intent)
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        when (error["status_code"]) {
            422 -> {
                Log.i("Logout", "Logout Error, Bad Data")
            }
            401 -> {
                Log.i("GoogleAuth", "Check Login Error, Bad Token")
                params?.put("response_obj", GoogleAuthResponse())
                params?.put("method", method)
                UpdateToken.updateToken(context, url, params)
            }
            500 -> {
                Log.i("Logout", "Logout Error, Server Error")
            }
            400 -> {
                Log.i("Logout", "Logout not Found")
            }
            404 -> {
                Log.i("Logout", "Logout Error, User not found")
            }
            else -> {
                Log.i("Logout", "Logout Error, Unknown Error")
            }
        }
    }
}