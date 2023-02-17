package com.yoshione.fingen.interfaces

import android.content.Context
import okhttp3.Request
import org.json.JSONObject

interface InterfaceRequests {
    fun request(
        context: Context,
        url: String,
        params: JSONObject?,
        myResponse: InterfaceNewResponse
    )

}
interface InterfaceNewRequests {
    fun newRequest(
        context: Context,
        url: String,
        method: String,
        params: JSONObject?,
        myResponse: InterfaceNewResponse
    )
}

interface InterfaceSendRequests {
    fun sendRequest(
        context: Context,
        request: Request,
        myResponse: InterfaceNewResponse
    )
}