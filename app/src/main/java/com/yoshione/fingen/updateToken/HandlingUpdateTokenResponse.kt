package com.yoshione.fingen.updateToken

import android.content.ContentValues
import android.content.Context
import com.yoshione.fingen.DBHelper
import okhttp3.Headers
import org.json.JSONObject

class HandlingUpdateTokenResponse {

    companion object {

        fun handlingUpdateToken(context: Context, response: JSONObject): Boolean {
            val headers = response.get("headers") as Headers
            val accessToken = headers.values("access_token")[0].toString()

            // запишем в бд
            val db = DBHelper(context)
            val dbh = db.writableDatabase
            val cursor = dbh.query(
                "cookies",
                null,
                null,
                null,
                null,
                null,
                null
            )

            try {
                if (cursor.moveToFirst()) {
                    // обновить токен
                    val cv = ContentValues()
                    cv.put("access_token", accessToken)
                    dbh.update("cookies", cv, null, null)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return false

            } finally {
                cursor.close()
                dbh.close()
                db.close()
            }
            return true
        }
    }
}