package com.yoshione.fingen.updateToken

import android.content.Context
import com.yoshione.fingen.NewRequest
import com.yoshione.fingen.database.GetDataFromDb
import com.yoshione.fingen.interfaces.InterfaceNewRequests
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class UpdateTokenRequest(private val urlRequest: String?, private val paramsRequest: JSONObject?):
    InterfaceNewRequests {

    override fun newRequest(
        context: Context,
        url: String,
        method: String,
        params: JSONObject?,
        myResponse: InterfaceNewResponse
    ) {
        val media = "application/json".toMediaTypeOrNull()
        val jsonBody = RequestBody.create(media, params.toString())

        val request = Request.Builder()
            .url(url)
            .tag(paramsRequest?.put("url", urlRequest))
            .method(method, jsonBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader(
                "Authorization", GetDataFromDb(context).getData(
                    context, "cookies", "refresh_token"
                ).toString()
            )
            .build()
        NewRequest().sendRequest(context, request, myResponse)
    }
}