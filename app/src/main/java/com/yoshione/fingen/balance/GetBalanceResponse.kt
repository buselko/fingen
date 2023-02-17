package com.yoshione.fingen.balance

import android.content.Context
import android.content.Intent
import android.util.Log
import com.yoshione.fingen.BalanceActivity
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class GetBalanceResponse : InterfaceNewResponse {
    var str = ""

    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
        val balance: JSONObject = response["balance"] as JSONObject
        val message = balance["message"]

        str = "Ваш баланс $message"
        val intent: Intent = Intent(context, BalanceActivity::class.java)
        intent.putExtra("balance", str)
        context.startActivity(intent)
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
                Log.i("GetBalance", "GetBalance Error, Bad Login or Password")
            }
            401 -> {
                params?.put("response_obj", GetBalanceResponse())
                params?.put("method", method)
                UpdateToken.updateToken(context, url, params)
                Log.i("GetBalance", "GetBalance Error, Bad password for email")
            }
            500 -> {
                Log.i("GetBalance", "GetBalance Error, Server Error")
            }
            400 -> {
                val intent: Intent = Intent(context, BalanceActivity::class.java)
                intent.putExtra("balance", "Не удалось получить баланс")
                context.startActivity(intent)
                Log.i("GetBalance", "Balance not Found")
            }
            404 -> {
                Log.i("GetBalance", "GetBalance Error, User not found")
            }
            else -> {
                Log.i("GetBalance", "GetBalance Error, Unknown Error")
            }
        }
    }

}