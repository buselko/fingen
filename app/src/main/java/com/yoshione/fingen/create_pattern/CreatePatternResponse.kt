package com.yoshione.fingen.create_pattern

import android.content.Context
import android.util.Log
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.patterns.AddPatternResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONArray
import org.json.JSONObject


class CreatePatternResponse : InterfaceNewResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        val notifications = response["notifications"]
        val notificationsArray = notifications as JSONArray

        HandlingReceivedNotifications(context, notificationsArray).startHandling()
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        if (error["status_code"] == 422) {
            Log.i("CreatePattern", "CreatePattern Error, Bad pattern or email")
        }

        if (error["status_code"] == 401) {
            params?.put("response_obj", AddPatternResponse())
            params?.put("method", method)
            UpdateToken.updateToken(context, url, params)
            Log.i("CreatePattern", "CreatePattern Error, Bad access token")
        }

        if (error["status_code"] == 500) {
            Log.i("CreatePattern", "CreatePattern Error, Server Error")
        }

        if (error["status_code"] == 404) {
            Log.i("CreatePattern", "CreatePattern Error, User not found")
        }
    }
}