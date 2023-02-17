package com.yoshione.fingen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yoshione.fingen.registration.RegistrationRequest
import com.yoshione.fingen.registration.RegistrationResponse
import org.json.JSONObject

class ActivityRegistration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    fun buttonRegistration(view: View) {
        val name = findViewById<EditText>(R.id.PersonNameRegistration)
        val email = findViewById<EditText>(R.id.EmailAddressRegistration)
        val password = findViewById<EditText>(R.id.PasswordRegistration)

        // check input data
        if (name.text.toString() == "" ||
            email.text.toString() == "" ||
            password.text.toString() == ""
        ) {
            val selection = findViewById<TextView>(R.id.textViewRegistration)
            selection.text = "Заполните все поля"
        } else {


            sendDataToServer(name, email, password)

            val intent = Intent(this, ConfirmActivity::class.java)
            intent.putExtra("email", email.text.toString())
            startActivity(intent)
        }
    }

    private fun sendDataToServer(name: EditText, email: EditText, password: EditText) {
        // send data to server

        val url = getString(R.string.server_url) +
                getString(R.string.prefixApi) +
                getString(R.string.endpoint_registration)

        val params = HashMap<String, String>()
        params["name"] = name.text.toString()
        params["email"] = email.text.toString()
        params["password"] = password.text.toString()

        val jsonObject = JSONObject(params as Map<*, *>)


        val response = RegistrationResponse()
        RegistrationRequest().newRequest(this, url, "POST", jsonObject, response)
    }
}