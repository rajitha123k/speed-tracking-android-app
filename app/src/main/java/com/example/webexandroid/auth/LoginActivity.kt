package com.example.webexandroid.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.webexandroid.databinding.ActivityLoginBinding
import com.example.webexandroid.R
import com.example.webexandroid.WebexAndroidApp
import com.example.webexandroid.utils.SharedPrefUtils.getEmailPref
import com.example.webexandroid.utils.SharedPrefUtils.saveEmailPref

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
            .also { binding = it }
            .apply {

                textEmailAddress.setText(getEmailPref(this@LoginActivity))

                btnOauthLogin.setOnClickListener {

                    var emailaddr = textEmailAddress.text
                    if (emailaddr.isEmpty()) {
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                getApplicationContext(),
                                "Please enter email address for user",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                        return@setOnClickListener
                    }

                    saveEmailPref(this@LoginActivity, textEmailAddress.text.toString())
                    startOAuthActivity()
                }
            }
    }

    private fun startOAuthActivity() {
        (application as WebexAndroidApp).loadKoinModules()
        startActivity(Intent(this@LoginActivity, OAuthWebLoginActivity::class.java))
        finish()
    }
}