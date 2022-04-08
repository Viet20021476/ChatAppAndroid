package com.example.chattingapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login_form_2.*


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginPreferences: SharedPreferences
    private lateinit var loginPrefsEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_login_form_2)
        init()
        handleEvent()

    }

    fun init() {
        auth = FirebaseAuth.getInstance()
        loginPreferences = getSharedPreferences("myPref", MODE_PRIVATE)
        loginPrefsEditor = loginPreferences.edit()

        edt_email.setText(loginPreferences.getString("email", ""))
        edt_password.setText(loginPreferences.getString("password", ""))
    }

    fun handleEvent() {
        tv_signup.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                var intent: Intent = Intent(applicationContext, SignUpActivity::class.java)
                startActivity(intent)
            }

        })

        btn_login.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                SignIn()
            }

        })

        login_form.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                }
                return true
            }

        })
    }

    fun SignIn() {

        var email: String = edt_email.text.toString()
        var password: String = edt_password.text.toString()

        if (!email.isEmpty() && !password.isEmpty()) {

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // Sign in success, update UI with the signed-in user's information
                        var intent: Intent = Intent(this, UserListActivity::class.java)
                        var bundle: Bundle = Bundle()
                        bundle.putString("key_uid", auth.currentUser?.uid)
                        intent.putExtras(bundle)
                        startActivity(intent)
                        edt_email.setText("")
                        edt_password.setText("")

                        if (cb_saveLogin.isChecked) {
                            loginPrefsEditor.putString("email", email);
                            loginPrefsEditor.putString("password", password);
                            loginPrefsEditor.commit();

                            Log.d("checkk", email)
                        }

                        cb_saveLogin.isChecked = false

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            applicationContext,
                            "Email hoặc password không đúng!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        } else {
            Toast.makeText(
                applicationContext,
                "Thông tin không được để trống!",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onStop() {
        Log.d("Vit", "login activity Stop")
        super.onStop()
    }

    override fun onDestroy() {

        Log.d("Vit", "login activity Destroy")
        super.onDestroy()
    }
}