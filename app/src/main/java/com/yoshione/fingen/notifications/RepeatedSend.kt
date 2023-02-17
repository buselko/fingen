package com.yoshione.fingen.notifications

import android.content.ContentValues
import android.content.Context
import com.yoshione.fingen.DBHelper
//import com.yoshione.fingen.main.crashlytics
import org.json.JSONObject

class RepeatedSend {

    fun addNotifyInQueue(context: Context, params: JSONObject, url: String) {
        val db = DBHelper(context)
        val dbh = db.writableDatabase
        val contentValues = ContentValues()

        contentValues.put("params", params.toString())
        contentValues.put("url", url)

        dbh.insert("notify_repeated_send", null, contentValues)
        dbh.close()
        db.close()

//        crashlytics.log("add notify in queue: $url")
    }

    fun deleteNotifyFromQueue(context: Context, params: String) {
        val db = DBHelper(context)
        val dbh = db.writableDatabase
        val query = "DELETE FROM notify_repeated_send WHERE params = ?"

        val statement = dbh.compileStatement(query)
        statement.bindString(1, params)
        statement.execute()
        statement.close()

        dbh.close()
        db.close()

//        crashlytics.log("delete notify from queue")
    }

    fun repeatedSendNextNotify(context: Context) {
        val db = DBHelper(context)
        val dbh = db.readableDatabase
        val cursor = dbh.rawQuery("SELECT * FROM notify_repeated_send", null)
        if (cursor.moveToFirst()) {
            val params = cursor.getString(1)
            val url = cursor.getString(2)

            cursor.close()
            dbh.close()
            db.close()

//            crashlytics.log("repeated send next notify: $url")
            val response = NotificationResponse()
            NotificationRequest().newRequest(
                context, url, "POST", JSONObject(params), response)
        }
        cursor.close()
        dbh.close()
        db.close()


    }


}