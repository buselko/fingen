package com.yoshione.fingen.login

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.ActivityMenuPro
import com.yoshione.fingen.interfaces.InterfaceNewResponse
//import com.yoshione.fingen.main.crashlytics
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class CheckLoginResponse : InterfaceNewResponse {

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
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

        if (error["status_code"] == 401) {
            Log.i("Check Login", "Check Login Error, Bad password for email")
            params?.put("response_obj", CheckLoginResponse())
            params?.put("method", method)
            UpdateToken.updateToken(context, url, params)
        } else {
//            crashlytics.recordException(Exception(error.toString()))
        }

        if (error["status_code"] == 422) {
            Log.i("Check Login", "Check Login Error, Bad Login or Password")
        }


        if (error["status_code"] == 500) {
            Log.i("Check Login", "Registration Error, Server Error")
        }

        if (error["status_code"] == 404) {
            Log.i("Check Login", "Registration Error, User not found")
        }


    }
}