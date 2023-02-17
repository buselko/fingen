package com.yoshione.fingen.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.yoshione.fingen.R
import com.yoshione.fingen.DBHelper
//import com.yoshione.fingen.main.crashlytics
import org.json.JSONObject


class NLService : NotificationListenerService() {
    private val tagLog: String = this.javaClass.simpleName
    private var mReceiver: NLServiceReceiver? = null
    private var notificationId = 0
    private var notificationText = ""
    private var sbnOld: StatusBarNotification? = null
    private val notifyId: Int = 2147483647
    private val notification = ArrayDeque<String>(10)


    override fun onCreate() {
        super.onCreate()
        mReceiver = NLServiceReceiver()
        val filter = IntentFilter()
        filter.addAction("com.example.NOTIFICATION_LISTENER_SERVICE_EXAMPLE")
        registerReceiver(mReceiver, filter)
        startForeground(notifyId, createForegroundNotify().build())
//        crashlytics.log("NLService started")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        startForeground(notifyId, createForegroundNotify().build())
//        crashlytics.recordException(Exception("NLService onTaskRemoved"))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
//        crashlytics.log("NLService destroyed")
    }

    private fun createForegroundNotify(): NotificationCompat.Builder {
        val builder = createNotify()
        builder.setOngoing(true)
        builder.setProgress(100, 0, true)
        return builder
    }

    private fun createNotify(): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, "com.sai.ANDROID")
        builder.setWhen(System.currentTimeMillis())
        builder.setTicker("Listener")
        builder.setContentText("Приложение работает...")
        builder.setContentTitle("Слушаю уведомления")
        builder.setSmallIcon(R.mipmap.ic_template_transfer)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        return builder
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {

        if (sbn.id == 557) {
            return
        }
        val notify = sbn.notification.extras.getCharSequence("android.text")
            .toString() + sbn.packageName + sbn.postTime.toString()
        if (notification.contains(notify)) {
            return
        } else {
            notification.add(notify)
        }
        if (sbn.notification.extras.getCharSequence("android.text") == notificationText) {
            return
        }

        if (sbn == sbnOld) {
            return
        }
        val summary = sbn.notification.extras.getCharSequence("android.summaryText").toString()
        if (summary != "null") {
            return
        }
        try {
            if (filterNotification(sbn)) {
                sendNotificationCode(sbn)
            }

            val intent = Intent("com.example.NOTIFICATION_LISTENER_EXAMPLE")
            intent.putExtra(
                "notification_event",
                "\n${
                    sbn.notification.extras.getCharSequence("android.text").toString()
                }\n" + sbn.packageName + "\n"
            )
            sendBroadcast(intent)

        } catch (e: Exception) {
            val infoIntent = Intent("com.example.NOTIFICATION_LISTENER_EXAMPLE")
            infoIntent.putExtra(
                "notification_event",
                "onNotificationPosted:" + e.message
            )

            sendBroadcast(infoIntent)
        }
        sbnOld = sbn
        notificationId = sbn.id
        notificationText = sbn.notification.extras.getCharSequence("android.text").toString()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i(tagLog, "onNotificationRemoved")
        Log.i(
            tagLog,
            "ID :${sbn.id}\n${
                sbn.notification.extras.getCharSequence("android.text").toString()
            }\n${sbn.packageName}"
        )
    }

    internal inner class NLServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("command") == "clearall") {
                cancelAllNotifications()
                val intentClearNotify = Intent("com.example.NOTIFICATION_LISTENER_EXAMPLE")
                intentClearNotify.putExtra("command", "clear text view")
                sendBroadcast(intentClearNotify)
            } else if (intent.getStringExtra("command") == "list") {
                val notificationIntent = Intent("com.example.NOTIFICATION_LISTENER_EXAMPLE")
                notificationIntent.putExtra("notification_event", "\n=======\n")
                sendBroadcast(notificationIntent)
                var i = 1
                for (sbn in this@NLService.activeNotifications) {
                    val infoIntent = Intent("com.example.NOTIFICATION_LISTENER_EXAMPLE")
                    infoIntent.putExtra(
                        "notification_event",
                        i.toString() + " " + sbn.packageName + "\n"
                    )
                    sendBroadcast(infoIntent)
                    i++
                }
                val listIntent = Intent("com.example.NOTIFICATION_LISTENER_EXAMPLE")
                listIntent.putExtra("notification_event", "\nNotification List\n")
                sendBroadcast(listIntent)
            }
        }
    }

    private fun sendNotificationCode(description: StatusBarNotification) {
        val thread = Thread(Runnable {
            val url = getString(R.string.server_url) +
                    getString(R.string.prefixApi) +
                    getString(R.string.endpoint_sendNotify)
            val params = HashMap<String, Any>()
            // создаём словарь параметров
            val notification = HashMap<String, String>()
            // получение id устройства

            if (description.notification.tickerText != null) {
                notification["ticker_text"] = description.notification.tickerText.toString()
            } else {
                notification["ticker_text"] = ""
            }

            notification["id"] = description.id.toString()
            notification["package"] = description.packageName
            notification["describe"] = description.notification.describeContents().toString()
            notification["text"] =
                description.notification.extras.getCharSequence("android.text").toString()
            notification["title"] =
                description.notification.extras.getCharSequence("android.title").toString()
            notification["subtext"] =
                description.notification.extras.getCharSequence("android.subText").toString()
            notification["info"] =
                description.notification.extras.getCharSequence("android.infoText").toString()
            notification["bigtext"] =
                description.notification.extras.getCharSequence("android.bigText").toString()
            notification["summary"] =
                description.notification.extras.getCharSequence("android.summaryText").toString()
            notification["time"] = description.postTime.toString()

            val notificationJson = JSONObject((notification as Map<*, *>?)!!)

            params["package_name"] = description.packageName
            params["notification_description"] = notificationJson

            val jsonObject = JSONObject(params as Map<*, *>)
            val repeatedSend = RepeatedSend()
            repeatedSend.addNotifyInQueue(this, jsonObject, url)

            val response = NotificationResponse()
            NotificationRequest().newRequest(
                this, url, "POST", jsonObject, response
            )
            Log.i("Notification Request", "Отправил запрос $params")
        })
        thread.start()


    }

    private fun filterNotification(sbn: StatusBarNotification): Boolean {
        val db = DBHelper(this)
        val dbh = db.readableDatabase
        val cursor = dbh.query(
            "apps",
            null,
            null,
            null,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val packageIndex = cursor.getColumnIndex("package_name")
            val isEnableIndex = cursor.getColumnIndex("isActive")
            do {
                if (sbn.packageName == cursor.getString(packageIndex)) {
                    if (cursor.getInt(isEnableIndex) == 1) {
                        cursor.close()
                        dbh.close()
                        db.close()
                        return true
                    }
                }
            } while (cursor.moveToNext())
        } else {
//            crashlytics.log("Not apps in database")
        }
        cursor.close()
        dbh.close()
        db.close()
        return false
    }
}
