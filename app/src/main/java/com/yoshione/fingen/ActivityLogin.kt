package com.yoshione.fingen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yoshione.fingen.login.CheckLoginRequest
import com.yoshione.fingen.login.CheckLoginResponse
import com.yoshione.fingen.login.LoginRequest
import com.yoshione.fingen.login.LoginResponse
import com.yoshione.fingen.notifications.NotificationUtils
import org.json.JSONObject
import java.util.*


class ActivityLogin : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        NotificationUtils(this)

        applyIntent()

//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))
        buttonSignUp()

        setButtonHomeMenu()
    }

    fun setButtonHomeMenu() {
        val buttonHomeMenu = findViewById<View>(R.id.buttonHomeMenu)
        buttonHomeMenu.setOnClickListener {
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
        }
    }

    private fun applyIntent() {
        if (intent.getStringExtra("error") != null) {
            Log.i("error", "error")
        } else {
            checkLogin()
        }
    }

    fun buttonSignIn(view: View) {
        val email = findViewById<EditText>(R.id.EmailAddressLogin)
        val password = findViewById<EditText>(R.id.PasswordLogin)
        // check input data
        if (email.text.toString() == "" || password.text.toString() == "") {
            val selection = findViewById<TextView>(R.id.textViewLogin)
            selection.text = "Заполните все поля"
        } else {
            loginToServer(email, password)
        }
    }

    private fun buttonSignUp() {
        val signupListener = findViewById<View>(R.id.SignUp)
        signupListener.setOnClickListener {
            val intent = Intent(this, ActivityRegistration::class.java)
            startActivity(intent)
        }
    }

    private fun checkLogin() {
        val url =
            getString(R.string.server_url) +
                    getString(R.string.prefixApi) +
                    getString(R.string.endpoint_check_login)

        val params = HashMap<String, String>()
        val jsonObject = JSONObject(params as Map<*, *>)


        val response = CheckLoginResponse()
        val thread = Thread(Runnable {
            CheckLoginRequest().newRequest(this, url, "POST", jsonObject, response)
        })
        thread.start()
    }

    private fun loginToServer(email: EditText, password: EditText) {
        // send data to server
        val url = getString(R.string.server_url) +
                getString(R.string.prefixApi) +
                getString(R.string.endpoint_login)

        val params = HashMap<String, String>()
        params["email"] = email.text.toString()
        params["password"] = password.text.toString()
        val jsonObject = JSONObject(params as Map<*, *>)


        val response = LoginResponse()
        val thread = Thread(Runnable {
            LoginRequest().newRequest(this, url, "POST", jsonObject, response)
        })
        thread.start()
    }


}