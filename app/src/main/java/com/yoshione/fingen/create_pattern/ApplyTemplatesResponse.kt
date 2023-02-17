package com.yoshione.fingen.create_pattern

import android.content.Context
import android.util.Log
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class ApplyTemplatesResponse : InterfaceNewResponse {

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        Log.i("ApplyTemplates", "ApplyTemplates Success")
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
                Log.i("ApplyTemplates", "GetBalance Error, Bad Login or Password")
            }
            401 -> {
                params?.put("response_obj", ApplyTemplatesResponse())
                UpdateToken.updateToken(context, url, params)
                Log.i("ApplyTemplates", "ApplyTemplates Error, Access Token is expired")
            }
            500 -> {
                Log.i("ApplyTemplates", "ApplyTemplates Error, Server Error")
            }
            400 -> {
                Log.i("ApplyTemplates", "ApplyTemplates not Found")
            }
            404 -> {
                Log.i("ApplyTemplates", "ApplyTemplates Error, User not found")
            }
            else -> {
                Log.i("ApplyTemplates", "ApplyTemplates Error, Unknown Error")
            }
        }
    }
}