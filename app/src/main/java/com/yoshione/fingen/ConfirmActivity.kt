package com.yoshione.fingen

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yoshione.fingen.confirmRegistration.ConfirmRequest
import com.yoshione.fingen.confirmRegistration.ConfirmResponse
import org.json.JSONObject

class ConfirmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)
    }

    fun buttonConfirm(view: View) {
        val confirmCode: String =
            findViewById<EditText>(R.id.EmailCodeForRegistration).text.toString()

        val email = intent.getStringExtra("email")!!

        if (confirmCode == "") {
            val select = findViewById<TextView>(R.id.textViewConfirmRegistration)
            select.text = "Введите код подтверждения"
        } else {
            sendCodeToServer(confirmCode, email)
        }
    }

    private fun sendCodeToServer(confirmCode: String, email: String) {
        val url = getString(R.string.server_url) +
                getString(R.string.prefixApi) +
                getString(R.string.endpoint_confirm)

        val params = HashMap<String, String>()
        params["code"] = confirmCode
        params["email"] = email
        val jsonObject = JSONObject(params as Map<*, *>)

        if ((params["code"] == null) || (params["email"] == null)) {
        } else {
        }

        val response = ConfirmResponse()
        ConfirmRequest().newRequest(this, url, "POST", jsonObject, response)
    }

}