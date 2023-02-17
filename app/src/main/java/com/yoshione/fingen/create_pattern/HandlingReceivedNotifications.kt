package com.yoshione.fingen.create_pattern

import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

class HandlingReceivedNotifications(
    val context: Context,
    val notifications: JSONArray
) {

    fun startHandling() {
        val descriptions = ArrayList<NotificationInfo>()
        for (i in 0 until notifications.length()) {
            val notification = notifications.getJSONObject(i) as JSONObject
            val packageName = notification.getString("package_name")
            val pattern = notification["notification_description"] as JSONObject
            descriptions.add(
                NotificationInfo(
                    notification.getString("id"),
                    pattern.getString("title"),
                    pattern.getString("title") + " " + pattern.getString("text"),
                    packageName,
                    false,
                    null
                )
            )

        }

        val intent = Intent("get notifications")
        intent.putExtra("command", "apply patterns")
        intent.putExtra("list", descriptions)
        context.sendBroadcast(intent)
    }


}