package com.yoshione.fingen.interfaces

import android.content.Context
import org.json.JSONObject

interface InterfaceSuccessResponse {

    fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String? = null,
        params: JSONObject? = null
    )
}

interface InterfaceNewErrorResponse {
    fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String? = null,
        params: JSONObject? = null
    )

}

interface InterfaceNewResponse: InterfaceNewErrorResponse, InterfaceSuccessResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    )

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    )
}