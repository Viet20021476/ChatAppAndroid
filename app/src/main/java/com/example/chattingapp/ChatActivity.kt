package com.example.chattingapp

import adapter.MSGAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import model.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var msgList: MutableList<model.Message>
    private lateinit var msgAdapter: MSGAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMsg: EditText
    private lateinit var btnSend: Button
    private lateinit var dbRef: DatabaseReference

    var senderRoom: String = ""
    var receiverRoom: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        init()
        handleEvent()


    }

    fun init() {
        edtMsg = findViewById(R.id.edt_msg)
        btnSend = findViewById(R.id.btn_send)
        msgList = mutableListOf()
        recyclerView = findViewById(R.id.rcv_msg)
        msgAdapter = MSGAdapter(this, msgList)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        recyclerView.adapter = msgAdapter
        recyclerView.layoutManager = linearLayoutManager

        var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        dbRef = firebaseDatabase.reference

        val senderUID = FirebaseAuth.getInstance().currentUser?.uid

        var name: String = intent.extras?.get("name") as String
        tv_name.text = name
        var receiverUID = intent.extras?.get("uid") as String

        senderRoom = receiverUID + senderUID
        receiverRoom = senderUID + receiverUID

    }

    fun handleEvent() {

        btnSend.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val msg: String = edtMsg.text.toString()

                if (msg.trim() != "") {
                    val time: String = SimpleDateFormat("HH:mm").format(Date())
                    var currUser = FirebaseAuth.getInstance().currentUser
                    if (currUser != null) {
                        msgList.add(Message(msg, time, currUser.uid))
                        msgAdapter.notifyItemInserted(msgList.size - 1)
                        val msgObj = Message(msg, time, currUser.uid)
                        dbRef.child("messages").child(senderRoom).push()
                            .setValue(msgObj).addOnSuccessListener {
                                dbRef.child("messages").child(receiverRoom).push()
                                    .setValue(msgObj)
                            }
                    }

                }
                edtMsg.setText("")
            }

        })

        dbRef.child("messages").child(senderRoom).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                msgList.clear()
                for (postSnapshot in snapshot.children) {
                    var msg: Message? = postSnapshot.getValue(Message::class.java)

                    if (msg != null) {
                        msgList.add(msg)
//                        Log.d("Check", msg.getText())
//                        Log.d("Check", msg.getTime())
//                        Log.d("Check", msg.getSenderUID())
                    }
                }
                msgAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        dbRef.child("messages").child(receiverRoom).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                msgList.clear()
                for (postSnapshot in snapshot.children) {
                    var msg: Message? = postSnapshot.getValue(Message::class.java)

                    if (msg != null) {
                        msgList.add(msg)
//                        Log.d("Check", msg.getText())
//                        Log.d("Check", msg.getTime())
//                        Log.d("Check", msg.getSenderUID())
                    }
                }
                msgAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getScreenWidth(): Int {
        var displayMetrics: DisplayMetrics = resources.displayMetrics

        return displayMetrics.widthPixels
    }
}


