package com.example.chattingapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_signup_2.*
import kotlinx.android.synthetic.main.activity_userlist.*
import model.User
import java.io.ByteArrayOutputStream

class SignUpActivity : AppCompatActivity() {
    private lateinit var img: CircleImageView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_2)

        init()
        handleEvent()
    }

    fun init() {
        auth = FirebaseAuth.getInstance()
        img = profile_pic
    }

    fun handleEvent() {

        tb_signup.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                onBackPressed()
            }

        })
        btn_signup.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                SignUp()
            }

        })

        profile_pic.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                ImagePicker.with(this@SignUpActivity)
//                    .crop()                    //Crop image(Optional), Check Customization for more option
//                    .compress(1024)            //Final image size will be less than 1 MB(Optional)
//                    .maxResultSize(
//                        1080,
//                        1080
//                    )    //Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
            }

        })

        signup_form.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                return true
            }

        })


    }

    fun SignUp() {
        var email: String = edt_email.text.toString()
        var password: String = edt_password.text.toString()
        var name: String = edt_user_name.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(
                            applicationContext,
                            "Đăng ký tài khoản thành công!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        edt_email.setText("")
                        edt_password.setText("")
                        edt_user_name.setText("")

                        var currUser = auth.currentUser

                        if (currUser != null) {
                            addUsertoDB(User(name, email, password, currUser.uid))
                        }


                        val storage = Firebase.storage("gs://feisty-flow-326908.appspot.com")

                        val storageRef = storage.reference

                        var imgName: String = "image$email"
                        val mountainsRef = storageRef.child("${imgName}.png")

                        val mountainImagesRef = storageRef.child("images/${imgName}.png")

                        img.isDrawingCacheEnabled = true
                        img.buildDrawingCache()
                        val bitmap = (img.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        val data = baos.toByteArray()

                        var uploadTask = mountainsRef.putBytes(data)
                        uploadTask.addOnFailureListener {
                            // Handle unsuccessful uploads
                        }.addOnSuccessListener { taskSnapshot ->
                            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                            // ...
                        }
                        onBackPressed()

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            applicationContext,
                            "Đăng ký tài khoản thất bại!",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                    }

                }
        }


    }

    fun addUsertoDB(user: User) {
        var dbRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(user.uid)
        dbRef.setValue(user)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var uri: Uri? = data?.data
        Log.d("uri", uri.toString())
        img.setImageURI(uri)

    }

    override fun onStop() {
        Log.d("Vit", "signup activity Stop")
        super.onStop()
    }

    override fun onDestroy() {

        Log.d("Vit", "sign up activity Destroy")
        super.onDestroy()
    }
}

