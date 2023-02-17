package com.yoshione.fingen.registration

import android.content.ContentValues
import android.content.Context
import com.yoshione.fingen.DBHelper
//import com.yoshione.fingen.main.crashlytics
import okhttp3.Headers
import org.json.JSONObject

class HandlingRegistrationResponse {

    companion object {
        fun handlingRegistration(context: Context, response: JSONObject): Boolean {
            val email = response.get("email") as String
            val headers = response.get("headers") as Headers
            val refreshToken = headers.values("refresh_token")[0].toString()
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
            val cv = ContentValues()

            cv.put("access_token", accessToken)
            cv.put("refresh_token", refreshToken)
            cv.put("email", email)
            try {
                if (cursor.moveToFirst()) {
                    dbh.update("cookies", cv, null, null)
                } else {
                    dbh.insert("cookies", null, cv)
                }
//                crashlytics.log("registration success")

            } catch (e: Exception) {
//                crashlytics.recordException(e)

            } finally {
                cursor.close()
                dbh.close()
                db.close()
            }
            return true
        }
    }
}