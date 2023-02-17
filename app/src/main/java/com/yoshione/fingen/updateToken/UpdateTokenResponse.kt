package com.yoshione.fingen.updateToken

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.ActivityLogin
import com.yoshione.fingen.NewRequest
import com.yoshione.fingen.database.GetDataFromDb
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class UpdateTokenResponse : InterfaceNewResponse {

    private var needMethod = ""

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        HandlingUpdateTokenResponse.handlingUpdateToken(context, response)

        val responseObj = params?.get("response_obj") as InterfaceNewResponse?
        params?.remove("response_obj")

        val methods = params?.get("method") as String?
        if (methods != null) {
            needMethod = methods
            params?.remove("method")
        } else {
            needMethod = method
        }

        if (url != null && responseObj != null) {
            params?.remove("response_obj")
            val request = prepareRequest(params, url, needMethod, context)
            NewRequest().sendRequest(context, request, responseObj)
        }
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {

        if (error["status_code"] == 401) {
            val intent = Intent(context, ActivityLogin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("error", "UpdateToken Error, Bad Token")
            context.startActivity(intent)
            Log.i("UpdateToken", "UpdateToken Error, Bad Token")
        }

        if (error["status_code"] == 422) {
            Log.i("AddPattern", "Notification Error, Bad Login or Password")
        }

        if (error["status_code"] == 500) {
            Log.i("AddPattern", "Notification Error, Server Error")
        }

        if (error["status_code"] == 404) {
            Log.i("AddPattern", "Notification Error, User not found")
        }
    }

    private fun prepareRequest(
        params: JSONObject?,
        url: String,
        method: String,
        context: Context
    ): Request {
        val media = "application/json".toMediaTypeOrNull()
        val jsonBody = RequestBody.create(media, params.toString())
        val accessToken =
            GetDataFromDb(context).getData(
                context, "cookies", "access_token"
            ).toString()

        return Request.Builder()
            .url(url)
            .tag(params?.put("url", url))
            .method(method, jsonBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Authorization", accessToken)
            .build()
    }

}