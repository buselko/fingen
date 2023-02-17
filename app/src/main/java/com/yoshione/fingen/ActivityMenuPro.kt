package com.yoshione.fingen

import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.yoshione.fingen.database.GetDataFromDb
import com.yoshione.fingen.login.LogOutRequest
import com.yoshione.fingen.login.LogOutResponse
import org.json.JSONObject

class ActivityMenuPro : AppCompatActivity() {


    private var mInfoTextView: TextView? = null
    private var mReceiver: NotificationBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        title = "TITLE"
        mInfoTextView = findViewById<TextView>(R.id.textView)
        mReceiver = NotificationBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction("com.example.NOTIFICATION_LISTENER_EXAMPLE")
        registerReceiver(mReceiver, filter)

        checkPermissionListenNotifications()

        setButtons()
    }

    private fun checkPermissionListenNotifications() {
        if (!NotificationManagerCompat.getEnabledListenerPackages(this)
                .contains(this.packageName)
        ) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }
    }

    private fun setButtons() {
        setButtonHomeMenu()
        setButtonSecond()
        setButtonSignOut()
    }

    private fun setButtonHomeMenu() {
        val homeMenuButton = findViewById<Button>(R.id.homeMenu2Button)
        homeMenuButton.setOnClickListener {
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
        }
    }

    private fun setButtonSignOut() {
        val signOutButton = findViewById<Button>(R.id.signOut)
        signOutButton.setOnClickListener {
            signOut()
        }
    }

    private fun setButtonSecond() {
        val secondButton = findViewById<Button>(R.id.googleAccountButton)
        secondButton.setOnClickListener {
            val intent = Intent(this, ActivityGoogleAuth::class.java)
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    internal inner class NotificationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("command") == "clear text view") {
                mInfoTextView!!.text = ""
            } else {
                val temp = intent.getStringExtra("notification_event") + "\n" + mInfoTextView!!.text
                mInfoTextView!!.text = temp
            }
        }
    }

    private fun signOut() {

        deleteAccount(this)

        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)
    }

    fun openThirdActivity(view: View) {
        val intent = Intent(this, ActivityListApps::class.java)
        startActivity(intent)
    }

    private fun deleteAccount(context: Context) {

        val thread = Thread(Runnable {
            deleteTokens()

            val db = DBHelper(context)
            val dbh = db.writableDatabase
            val cv = ContentValues()

            cv.put("access_token", "")
            cv.put("refresh_token", "")
            cv.put("email", "")

            dbh.update("cookies", cv, null, null)
            dbh.close()
            db.close()
        })
        thread.start()

    }

    fun buttonBalanceMenu(view: View) {
        val intent = Intent(this, BalanceActivity::class.java)
        startActivity(intent)
    }

    private fun deleteTokens() {
        val url = this.getString(R.string.server_url) +
                this.getString(R.string.prefixApi) +
                this.getString(R.string.endpoint_logout)

        val email = GetDataFromDb(this).getData(
            this, "cookies", "email"
        ).toString()

        val params = HashMap<String, String>()
        params["email"] = email
        val jsonObject = JSONObject(params as Map<*, *>)


        val response = LogOutResponse()
        LogOutRequest().newRequest(this, url, "POST", jsonObject, response)
    }

}