package com.yoshione.fingen.notifications

import android.content.Context
import android.util.Log
import com.yoshione.fingen.interfaces.InterfaceNewResponse
import com.yoshione.fingen.updateToken.UpdateToken
import org.json.JSONObject

class NotificationResponse :
    InterfaceNewResponse {
    override fun responseSuccess(
        context: Context,
        response: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {

        val thread = Thread(Runnable {
            val repeatedSend = RepeatedSend()
            repeatedSend.deleteNotifyFromQueue(context, params.toString())
            repeatedSend.repeatedSendNextNotify(context)
        })
        thread.start()
    }

    override fun responseError(
        context: Context,
        error: JSONObject,
        method: String,
        url: String?,
        params: JSONObject?
    ) {
      try {

          if (error["status_code"] == 422) {
              Log.i("Notification", "Notification Error, Bad Login or Password")
          }

          if (error["status_code"] == 401) {
              params?.put("response_obj", NotificationResponse())
              params?.put("method", method)
              UpdateToken.updateToken(context, url, params)
              Log.i("Notification", "Notification Error, Bad password for email")
          }

          if (error["status_code"] == 500) {
              Log.i("Notification", "Notification Error, Server Error")
          }

          if (error["status_code"] == 404) {
              Log.i("Notification", "Notification Error, User not found")
          }
      } catch (_: NullPointerException) {
          Log.i("Notification", "Notification Error, NullPointerException")
      }
    }
}