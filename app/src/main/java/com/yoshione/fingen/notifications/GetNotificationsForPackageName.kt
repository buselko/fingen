package com.yoshione.fingen.notifications

import android.content.Context
import com.yoshione.fingen.R
import com.yoshione.fingen.create_pattern.CreatePatternResponse
import com.yoshione.fingen.database.GetDataFromDb
//import com.yoshione.fingen.main.crashlytics
import org.json.JSONObject

class GetNotificationsForPackageName {

    fun getNotifications(context: Context, packageName: String) {
        try {
            val thread = Thread(Runnable {

                val url = context.getString(R.string.server_url) +
                        context.getString(R.string.prefixApi) +
                        context.getString(R.string.endpoint_get_notifications)
                val params = HashMap<String, String>()
                params["email"] = GetDataFromDb(context).getData(
                    context, "cookies", "email"
                ).toString()
                params["package_name"] = packageName

                val jsonObject = JSONObject(params as Map<*, *>)

//                crashlytics.log(" get notifications request for url: $url, $packageName")

                val response = CreatePatternResponse()
                NotificationRequest().newRequest(context, url, "POST", jsonObject, response)

            })
            thread.start()
        } catch (e: NullPointerException) {
//            crashlytics.recordException(e)
        }
    }
}