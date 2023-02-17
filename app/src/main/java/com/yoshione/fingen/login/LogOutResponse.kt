package com.yoshione.fingen.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import org.json.JSONObject

class LogOutResponse: InterfaceNewResponse {
    var str = ""

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        Toast.makeText(context, "Logout Success", Toast.LENGTH_SHORT).show()
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
                Log.i("Logout", "Logout Error, Bad Data")
            }
            401 -> {
                Log.i("Logout", "Logout Error")
            }
            500 -> {
                Log.i("Logout", "Logout Error, Server Error")
            }
            400 -> {
                Log.i("Logout", "Logout not Found")
            }
            404 -> {
                Log.i("Logout", "Logout Error, User not found")
            }
            else -> {
                Log.i("Logout", "Logout Error, Unknown Error")
            }
        }
    }

}