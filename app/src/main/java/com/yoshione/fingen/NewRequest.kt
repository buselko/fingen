package com.yoshione.fingen

import android.content.Context
import android.util.Log
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.interfaces.InterfaceSendRequests
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class NewRequest : InterfaceSendRequests {

    override fun sendRequest(
        context: Context,
        request: Request,
        myResponse: InterfaceNewResponse
    ) {
        val client = OkHttpClient()
        try {
            val response = client.newCall(request).execute()

            val responseJson = response.peekBody(99999L).string()
            val headers = response.headers

            val tag = request.tag() as JSONObject
            val urlRequest = tag["url"].toString()
            tag.remove("url")

            if (response.isSuccessful) {
                myResponse.responseSuccess(
                    context,
                    JSONObject(responseJson).put("headers", headers),
                    request.method,
                    urlRequest,
                    request.tag() as JSONObject?
                )
            } else {
                myResponse.responseError(
                    context,
                    JSONObject(responseJson).put("status_code", response.code),
                    request.method,
                    urlRequest,
                    request.tag() as JSONObject?
                )
            }
        } catch(e: Exception) {
            Log.i( "Request","error server $e")
        }
    }
}