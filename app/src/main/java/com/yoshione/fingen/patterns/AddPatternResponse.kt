package com.yoshione.fingen.patterns

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class AddPatternResponse : InterfaceNewResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        Toast.makeText(context, "Pattern added", Toast.LENGTH_SHORT).show()
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        if (error["status_code"] == 422) {
            Log.i("AddPattern", "Notification Error, Bad Login or Password")
        }

        if (error["status_code"] == 401) {
            params?.put("response_obj", AddPatternResponse())
            params?.put("method", method)
            UpdateToken.updateToken(context, url, params)
            Log.i("AddPattern", "Notification Error, Bad password for email")
        }

        if (error["status_code"] == 500) {
            Log.i("AddPattern", "Notification Error, Server Error")
        }

        if (error["status_code"] == 404) {
            Log.i("AddPattern", "Notification Error, User not found")
        }
    }
}