package com.yoshione.fingen.balance

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.BalanceActivity
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class CreateBalanceResponse : InterfaceNewResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        val intent: Intent = Intent(context, BalanceActivity::class.java)
        context.startActivity(intent)
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        if (error["status_code"] == 422) {
            Log.i("GetBalance", "GetBalance Error, Bad Login or Password")
        }

        if (error["status_code"] == 401) {
            params?.put("response_obj", CreateBalanceResponse())
            params?.put("method", method)
            UpdateToken.updateToken(context, url, params)
            Log.i("GetBalance", "GetBalance Error, Bad password for email")
        }

        if (error["status_code"] == 500) {
            Log.i("GetBalance", "GetBalance Error, Server Error")
        }

        if (error["status_code"] == 400) {
            val errorText = error["status_code"] as JSONObject
            val intent: Intent = Intent(context, BalanceActivity::class.java)
            intent.putExtra("balance", errorText["message"].toString())
            context.startActivity(intent)
            Log.i("GetBalance", "Balance not Found")
        }

        if (error["status_code"] == 404) {
            Log.i("GetBalance", "GetBalance Error, User not found")
        }
    }

}
