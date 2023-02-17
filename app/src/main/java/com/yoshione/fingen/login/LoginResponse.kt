package com.yoshione.fingen.login

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.ActivityMenuPro
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.registration.HandlingRegistrationResponse
import org.json.JSONObject

class LoginResponse : InterfaceNewResponse {

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        println(response)
        if (HandlingRegistrationResponse.handlingRegistration(context, response)) {

            val intent = Intent(context, ActivityMenuPro::class.java)
            context.startActivity(intent)
        }
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        if (error["status_code"] == 422) {
            Log.i("Login", "Registration Error, Bad Login or Password")
        }

        if (error["status_code"] == 401) {
            Log.i("Login", "Registration Error, Bad password for email")
        }

        if (error["status_code"] == 500) {
            Log.i("Login", "Registration Error, Server Error")
        }

        if (error["status_code"] == 404) {
            Log.i("Login", "Registration Error, User not found")
        }
    }
}