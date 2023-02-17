package com.yoshione.fingen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class ActivitySecond : AppCompatActivity() {
    private var gso: GoogleSignInOptions? = null
    private var gsc: GoogleSignInClient? = null
    var name: TextView? = null
    var email: TextView? = null
    private var signOutBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        signOutBtn = findViewById(R.id.signOut)

        gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        gsc = GoogleSignIn.getClient(this, gso!!)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val personName = account.displayName
            val personEmail = account.email
            name!!.text = personName
            email!!.text = personEmail
        }
        signOutBtn!!.setOnClickListener(View.OnClickListener { signOut() })
    }

    private fun signOut() {
        gsc!!.signOut().addOnCompleteListener {
            finish()
            startActivity(Intent(this@ActivitySecond, ActivityMenuPro::class.java))
        }
    }

    fun buttonBack(view: View) {
        val intent = Intent(this, ActivityMenuPro::class.java)
        startActivity(intent)
    }
}