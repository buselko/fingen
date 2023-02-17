package com.yoshione.fingen.confirmRegistration

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.ActivityMenuPro
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import org.json.JSONObject

class ConfirmResponse : InterfaceNewResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        val intent = Intent(context, ActivityMenuPro::class.java)
        context.startActivity(intent)

        Log.i("Registration confirm", "Registration confirm Success")
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        if (error["status_code"] == 422) {
            Log.i("Registration confirm", "Registration Error, Bad Login or Password")
        }

        if (error["status_code"] == 401) {
            Log.i("Registration confirm", "Registration Error, Bad password for email")
        }

        if (error["status_code"] == 500) {
            Log.i("Registration confirm", "Registration Error, Server Error")
        }
    }
}