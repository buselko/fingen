package com.yoshione.fingen.updateToken

import android.content.Context
import com.yoshione.fingen.R
import com.yoshione.fingen.database.GetDataFromDb
import org.json.JSONObject

class UpdateToken {

    companion object {
        fun updateToken(
            context: Context,
            url_request: String? = null,
            params_request: JSONObject? = null
        ) {
            // обновить токен
            val url = context.getString(R.string.server_url) +
                    context.getString(R.string.prefixApi) +
                    context.getString(R.string.endpoint_refresh_token)
            val email = GetDataFromDb(context).getData(context, "cookies", "email").toString()

            val params = HashMap<String, String>()
            params["email"] = email
            val jsonObject = JSONObject(params as Map<*, *>)
            val response = UpdateTokenResponse()
            UpdateTokenRequest(url_request, params_request).newRequest(
                context,
                url,
                "POST",
                jsonObject,
                response
            )
        }
    }
}