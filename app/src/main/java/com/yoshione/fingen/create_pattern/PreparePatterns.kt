package com.yoshione.fingen.create_pattern

import android.widget.ListView
import org.json.JSONArray
import org.json.JSONObject

class PreparePatterns {

    fun getPatterns(email: String, listV: ListView): JSONArray {

        val adapter = listV.adapter as NotificationsAdapter
        val patternsArray = JSONArray()
        for (i in 0 until adapter.count) {
            val notification = adapter.getItem(i) as NotificationInfo
            if (notification.isActive) {
                val pattern = JSONObject()
                pattern.put("id", notification.notify_id)
                pattern.put("pattern", notification.text)
                pattern.put("package_name", notification.packageName)
                pattern.put("email", email)
                pattern.put("action", adapter.getAction(i))
                patternsArray.put(pattern)
            }

        }
        return patternsArray
    }
}