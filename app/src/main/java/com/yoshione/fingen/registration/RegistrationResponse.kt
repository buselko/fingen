package com.yoshione.fingen.registration

import android.content.Context
import android.util.Log
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import org.json.JSONObject

class RegistrationResponse : InterfaceNewResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        HandlingRegistrationResponse.handlingRegistration(context, response)
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        if (error["status_code"] == 422) {
            Log.i("Registration", "Registration Error, Bad Login or Password")
        }

        if (error["status_code"] == 401) {
            Log.i("Registration", "Registration Error, Bad password for email")
        }

        if (error["status_code"] == 500) {
            Log.i("Registration", "Registration Error, Server Error")
        }

        if (error["status_code"] == 404) {
            Log.i("Registration", "Registration Error, User not found")
        }
    }
}