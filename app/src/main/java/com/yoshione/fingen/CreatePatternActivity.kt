package com.yoshione.fingen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.yoshione.fingen.create_pattern.NotificationInfo
import com.yoshione.fingen.create_pattern.NotificationsAdapter
import com.yoshione.fingen.create_pattern.SendPatternsInServer
import com.yoshione.fingen.notifications.GetNotificationsForPackageName

class CreatePatternActivity : AppCompatActivity() {
    private var notificationsAdapter: NotificationsAdapter? = null
    private var mReceiver: CreateTemplatesBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pattern)
        registerReceiverCommand()
        val packageName = intent.getStringExtra("package_name").toString()

        val listV = findViewById<View>(R.id.listNotification) as ListView

        GetNotificationsForPackageName().getNotifications(this, packageName)

        setListenerForButtons(listV)

    }

    private fun registerReceiverCommand() {
        mReceiver = CreateTemplatesBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction("get notifications")
        registerReceiver(mReceiver, filter)

    }

    private fun setListenerForButtons(listV: ListView) {
        val backButton = findViewById<View>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, ActivityListApps::class.java)
            startActivity(intent)
        }

        val applyTemplates = findViewById<View>(R.id.applyChanges)
        applyTemplates.setOnClickListener {
            SendPatternsInServer().sendTemplatesInServer(this, listV)

            val intent = Intent(this, ActivityListApps::class.java)
            startActivity(intent)
        }
    }

    inner class CreateTemplatesBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val listV = findViewById<View>(R.id.listNotification) as ListView
            if (intent.getStringExtra("command") == "apply patterns") {
//                // забираем список уведомлений
                val descriptions = intent.getParcelableArrayListExtra<NotificationInfo>("list")

                notificationsAdapter =
                    descriptions?.let { NotificationsAdapter(this@CreatePatternActivity, it) }
                listV.adapter = notificationsAdapter
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}