package com.example.chattingapp

import adapter.MSGAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_chat.*
import model.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var msgList: MutableList<model.Message>
    private lateinit var msgAdapter: MSGAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMsg: EditText
    private lateinit var ivSend: ImageView
    private lateinit var dbRef: DatabaseReference
    lateinit var receiverUID: String

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
        ivSend = findViewById(R.id.iv_send)
        msgList = mutableListOf()
        recyclerView = findViewById(R.id.rcv_msg)
        msgAdapter = MSGAdapter(this, msgList)
        var linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)

        recyclerView.adapter = msgAdapter
        recyclerView.layoutManager = linearLayoutManager

        var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        dbRef = firebaseDatabase.reference

        val senderUID = FirebaseAuth.getInstance().currentUser?.uid
        receiverUID = intent.extras?.get("uid") as String

        var name: String = intent.extras?.get("name") as String
        tv_name.text = name



        senderRoom = receiverUID + senderUID
        receiverRoom = senderUID + receiverUID

        val email: String = intent.extras?.get("email") as String

        val storage = Firebase.storage

        val storageRef = storage.reference

        val gsRef = storage.getReferenceFromUrl(
            "gs://feisty-flow-326908.appspot.com/image" +
                    "${email}.png"
        )

        gsRef.downloadUrl
            .addOnSuccessListener { urlImage ->
                Glide.with(this).load(urlImage).into(iv_profile_pic_chat)
            }
    }

    fun handleEvent() {

        ivSend.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val msg: String = edtMsg.text.toString()

                if (msg.trim() != "") {
                    val time: String = SimpleDateFormat("HH:mm").format(Date())
                    var currUser = FirebaseAuth.getInstance().currentUser
                    if (currUser != null) {
                        msgList.add(Message(msg, time, currUser.uid, receiverUID))
                        msgAdapter.notifyItemInserted(msgList.size - 1)
                        recyclerView.scrollToPosition(msgList.size - 1)
                        val msgObj = Message(msg, time, currUser.uid, receiverUID)
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

        iv_back.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                onBackPressed()
            }

        })

        rcv_msg.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                return true
            }

        })

        ln_chat.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                return true
            }

        })

        dbRef.child("messages").child(senderRoom)
            .addValueEventListener(object : ValueEventListener {

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
                    recyclerView.scrollToPosition(msgList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        dbRef.child("messages").child(receiverRoom)
            .addValueEventListener(object : ValueEventListener {

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
                    recyclerView.scrollToPosition(msgList.size - 1)
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


