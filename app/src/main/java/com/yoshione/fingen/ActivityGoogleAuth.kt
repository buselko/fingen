package com.yoshione.fingen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.yoshione.fingen.database.GetDataFromDb
import com.yoshione.fingen.login.googleAuth.GoogleAuthRequest
import com.yoshione.fingen.login.googleAuth.GoogleAuthResponse
import org.json.JSONObject


class ActivityGoogleAuth : AppCompatActivity() {
    //create array<string> of scopes
    val MyScopes = arrayOf(
        "https://www.googleapis.com/auth/spreadsheets",
        "https://www.googleapis.com/auth/drive",
        "https://www.googleapis.com/auth/drive.file"
    )
    var gso: GoogleSignInOptions? = null
    var gsc: GoogleSignInClient? = null

    private var signin: SignInButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_auth)
        signin = findViewById(R.id.sign_in_button)
        signin?.setOnClickListener { signIn() }
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(getString(R.string.server_client_id))
            .requestEmail()
            .requestScopes(Scope(MyScopes[0]))
            .requestScopes(Scope(MyScopes[1]))
            .requestScopes(
                Scope(MyScopes[2])
            ).build()

        gsc = GoogleSignIn.getClient(this, gso!!)
    }

    // get access token
    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val personEmail = account.email
            Toast.makeText(this, "$personEmail", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ActivitySecond::class.java)
            this.startActivity(intent)
        }

    }

    private fun signIn() {
        gsc?.signInIntent?.let { startActivityForResult(it, 101) }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val personToken = account?.serverAuthCode
            sendAuthCode(personToken.toString())
        } catch (e: ApiException) {

        }
    }

    private fun sendAuthCode(authCode: String) {
        val url = getString(R.string.server_url) +
                getString(R.string.prefixApi) +
                getString(R.string.endpoint_google_auth)
        val params = HashMap<String, String>()
        params["auth_code"] = authCode
        params["email"] = GetDataFromDb(this).getData(
            this, "cookies", "email"
        ).toString()
        val jsonObject = JSONObject(params as Map<*, *>)


        val response = GoogleAuthResponse()
        val thread = Thread(Runnable {
            GoogleAuthRequest().newRequest(this, url, "POST", jsonObject, response)
        })
        thread.start()
    }
}

