package com.yoshione.fingen.create_pattern

import android.content.Context
import android.widget.ListView
import com.yoshione.fingen.R
import com.yoshione.fingen.database.GetDataFromDb
import com.yoshione.fingen.notifications.NotificationRequest
import org.json.JSONArray
import org.json.JSONObject

class SendPatternsInServer {

    fun sendTemplatesInServer(context: Context, listV: ListView) {

        val thread = Thread(Runnable {

            val url = context.getString(R.string.server_url) +
                    context.getString(R.string.prefixApi) +
                    context.getString(R.string.endpoint_addPatterns)
            val params = HashMap<String, JSONArray>()
            val email = GetDataFromDb(context).getData(
                context, "cookies", "email"
            ).toString()
            params["patterns"] = PreparePatterns().getPatterns(email, listV)
            val jsonObject = JSONObject(params as Map<*, *>)


            val response = ApplyTemplatesResponse()
            NotificationRequest().newRequest(context, url, "POST", jsonObject, response)

        })
        thread.start()
    }
}