package com.example.chattingapp

import adapter.UserAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_userlist.*
import model.User

class UserListActivity : AppCompatActivity() {
    private lateinit var userList: MutableList<User>
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
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

        var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Vit", "hereeeeeee")
                userList.clear()

                for (postSnapShot in snapshot.children) {
                    var user = postSnapShot.getValue(User::class.java)

                    if (user?.uid.equals(text)) {
                        tv_gretting_text.setText("Hello, ${user?.name}")
                    }

                    if (auth.currentUser?.uid != user?.uid) {
                        if (user != null) {
                            userList.add(user)
                        }
                    }
                }

                userAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    fun handleEvent() {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.log_out) {
            auth.signOut()

            finish()
            var intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            //edt_password.setText("")
        }
        return super.onOptionsItemSelected(item)
    }
}