package com.example.swadaya

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.swadaya.MainActivity.SharedPreferencesUtil.isLoggedIn
import com.example.swadaya.MainActivity.SharedPreferencesUtil.saveTokenAndUserId
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val PREF_NAME = "MyPref"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isLoggedIn(this)) {
            navigateToHome()
        } else {
            initAction()
        }
//        initAction()
    }

    private fun initAction() {
        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {
            login(this)
        }
    }

    object ErrorUtils {
        fun parseError(response: Response<*>): String {
            return try {
                val errorBody = response.errorBody()?.string()
                val jsonObject = JSONObject(errorBody)
                jsonObject.getString("message")
            } catch (e: Exception) {
                "Terjadi kesalahan"
            }
        }
    }

    private fun login(context: Context) {
        val etEmail = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val request = UserRequest()

        request.username = etEmail.text.toString().trim()
        request.password = etPassword.text.toString().trim()

        val retro = Retro().getRetroClientInstance().create(UserApi::class.java)
        retro.login(request).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    val token = user?.data?.token
                    if (token != null) {
                        Log.e("token", token)
                        val userId = user?.data?.id
                        if (userId != null) {
                            Log.e("userId", userId.toString())
                            saveTokenAndUserId(context, token, userId)
                        }
                    } else {
                        Log.e("token", "Token is null")
                    }

                    val username = user?.data?.username
                    if (username != null) {
                        Log.e("username", username)
                        SharedPreferencesUtil.saveUsername(context, username)
                    } else {
                        Log.e("username", "Username is null")
                    }

                    val name = user?.data?.name
                    if (name != null) {
                        Log.e("name", name)
                    } else {
                        Log.e("name", "Name is null")
                    }

                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    user?.data?.username?.let {
                        intent.putExtra("USERNAME", it)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    handleUnsuccessfulResponse(response)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("Error", t.message ?: "Unknown error")
            }
        })
    }


    object SharedPreferencesUtil {
        private const val PREF_NAME = "MyPrefs"

        fun saveTokenAndUserId(context: Context, token: String, userId: Int) {
            val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("TOKEN", token)
                putInt("USER_ID", userId)
                apply()
            }
        }
        fun saveUsername(context: Context, username: String) {
            val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("USERNAME", username)
                apply()
            }
        }

        fun getUsername(context: Context): String {
            val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("USERNAME", "") ?: ""
        }

        fun isLoggedIn(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val token = sharedPref.getString("TOKEN", "")
            return token?.isNotEmpty() ?: false
        }
    }

    private fun handleUnsuccessfulResponse(response: Response<UserResponse>) {
        val errorMessage = if (response.code() == 401) {
            ErrorUtils.parseError(response)
        } else {
            "Login Gagal"
        }
        Log.e("Error", "Response not successful: $errorMessage")
        val tvLoginStatus = findViewById<TextView>(R.id.tvLoginStatus)
        tvLoginStatus?.let {
            it.visibility = View.VISIBLE
            it.text = errorMessage
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

}