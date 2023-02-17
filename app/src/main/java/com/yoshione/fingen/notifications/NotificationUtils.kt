package com.yoshione.fingen.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color

class NotificationUtils(base: Context?) : ContextWrapper(base) {
    private var mManager: NotificationManager? = null

    init {
        createChannels()
    }

    private fun createChannels() {

        // create android channel
        val androidChannel = NotificationChannel(
            ANDROID_CHANNEL_ID,
            ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true)
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true)
        // Sets the notification light color for notifications posted to this channel
        androidChannel.lightColor = Color.BLUE
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager!!.createNotificationChannel(androidChannel)

    }

    private val manager: NotificationManager?
        get() {
            if (mManager == null) {
                mManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return mManager
        }

    companion object {
        const val ANDROID_CHANNEL_ID = "com.sai.ANDROID"
        const val ANDROID_CHANNEL_NAME = "ANDROID CHANNEL"
    }
}