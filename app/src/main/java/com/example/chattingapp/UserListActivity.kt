package com.example.chattingapp

import adapter.UserAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_userlist.*
import model.User
import kotlinx.android.synthetic.main.activity_login_form_2.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class UserListActivity : AppCompatActivity() {
    private lateinit var userList: MutableList<User>
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userlist)

        init()
        handleEvent()


    }

    fun init() {
        userList = mutableListOf()
        auth = FirebaseAuth.getInstance()

        recyclerView = rcv_user
        userAdapter = UserAdapter(userList, this)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        recyclerView.adapter = userAdapter
        recyclerView.layoutManager = linearLayoutManager

        var text: String = intent.extras?.get("key_uid") as String

        dbRef = FirebaseDatabase.getInstance().getReference("users")

        dbRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                Log.d("thay doi","Thay doi")

                for (postSnapShot in snapshot.children) {
                    var user = postSnapShot.getValue(User::class.java)

                    if (user?.uid.equals(text)) {
                        tv_gretting_text.setText("${user?.name}")
                    }

                    if (auth.currentUser?.uid != user?.uid) {
                        if (user != null) {
                            userList.add(user)
                        }
                    }

                }
                userList.reverse()

                userAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        dbRef.orderByChild("timestamp").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("lll","Change")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val storage = Firebase.storage

        val storageRef = storage.reference

        val gsRef = storage.getReferenceFromUrl(
            "gs://feisty-flow-326908.appspot.com/image" +
                    "${FirebaseAuth.getInstance().currentUser?.email}.png"
        )

        gsRef.downloadUrl
            .addOnSuccessListener { urlImage ->
                Glide.with(this).load(urlImage).into(profile_pic_ul)
            }
    }


    fun handleEvent() {
        iv_logout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                onBackPressed()
            }

        })



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.log_out) {
//
//            finish()
//            var intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            //edt_password.setText("")
//        }
//        return super.onOptionsItemSelected(item)
//    }

    fun getRecyclerView() = recyclerView

    fun updateStatus(status: String) {
        var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        var haspMap: HashMap<String, String> = HashMap()
        haspMap.put("status", status)

        dbRef.updateChildren(haspMap as Map<String, Any>)
    }

    override fun onResume() {
        Log.d("Vit", "Resume")
        updateStatus("online")
        super.onResume()
    }

//    override fun onPause() {
//        updateStatus("offline")
//        Log.d("Vit", "Pause")
//        super.onPause()
//    }

    override fun onStop() {
        if (!ChatActivity.checkInChat) {
            updateStatus("offline")
            updateLastSeen(getCurrentDate())
            auth.signOut()

//            dbRef.removeValue()
//
//            Log.d("kkk",userList.size.toString())
//
//            for (user in userList) {
//                addUsertoDB(user)
//            }

        }

        Log.d("Vit", "user list activity Stop")
        super.onStop()
    }

    override fun onDestroy() {

        Log.d("destroy", "User list activity Destroy")
        super.onDestroy()
    }

    fun updateLastSeen(lastOnline: String) {
        var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        var haspMap: HashMap<String, String> = HashMap()
        haspMap.put("lastOnline", lastOnline)

        dbRef.updateChildren(haspMap as Map<String, Any>)
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }

    fun addUsertoDB(user: User) {
        var dbRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(user.uid)
        dbRef.setValue(user)
    }

    companion object {
        lateinit var recyclerView: RecyclerView
        lateinit var userAdapter: UserAdapter
    }

}