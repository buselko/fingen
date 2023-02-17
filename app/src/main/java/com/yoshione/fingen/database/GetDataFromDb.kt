package com.yoshione.fingen.database

import android.content.Context
import android.database.Cursor
import com.yoshione.fingen.DBHelper
import com.yoshione.fingen.interfaces.InterfaceGetData
//import com.yoshione.fingen.main.crashlytics
import io.requery.android.database.sqlite.SQLiteDatabase

class GetDataFromDb(context: Context) : InterfaceGetData {

    override fun getData(context: Context, table: String, key: String): String? {
        val db = DBHelper(context)
        // get token from db
        val dbh = db.writableDatabase
        // получаем данные приложений
        val cursor = dbh.query(table, null, null, null, null, null, null)
        // cursor must not be null
        try {
            if (cursor.moveToFirst()) {
                val tokenIndex = cursor.getColumnIndex(key)
                if (!cursor.isNull(tokenIndex)) {
                    val info = cursor.getString(tokenIndex)
//                    crashlytics.log("get data from database success")
                    return info
                } else {
//                    crashlytics.log("get data from database failed")
                    return null
                }
            } else {
//                crashlytics.log("get data from database failed")
                return null
            }
        } catch (e: Exception) {
//            crashlytics.recordException(e)
            return null
        }
        finally {
            close(db, dbh, cursor)
        }
    }

    private fun close(db: DBHelper, dbh: SQLiteDatabase, cursor: Cursor) {
        cursor.close()
        dbh.close()
        db.close()
    }

}