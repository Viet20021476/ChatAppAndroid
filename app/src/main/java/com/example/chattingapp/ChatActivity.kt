package com.example.chattingapp

import adapter.MSGAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_msg_receive.*
import kotlinx.android.synthetic.main.item_msg_send.*
import model.Message
import model.User
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private lateinit var msgList: MutableList<model.Message>
    private lateinit var msgAdapter: MSGAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMsg: EditText
    private lateinit var ivSend: ImageView
    private lateinit var dbRef: DatabaseReference
    lateinit var gsRef: StorageReference
    lateinit var receiverUID: String
    lateinit var userList: MutableList<User>
    var currPos: Int = 0
    var senderUID = "";
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


        //Create room for sender and receiver
        senderUID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        receiverUID = intent.extras?.get("uid") as String
        senderRoom = receiverUID + senderUID
        receiverRoom = senderUID + receiverUID

        var name: String = intent.extras?.get("name") as String
        tv_name.text = name

        userList = intent.extras?.getParcelableArrayList("user_list")!!

        currPos = intent.extras?.get("pos") as Int


        val email: String = intent.extras?.get("email") as String

        val storage = Firebase.storage

        val storageRef = storage.reference

        //Load user profile pic
        gsRef = storage.getReferenceFromUrl(
            "gs://feisty-flow-326908.appspot.com/image" +
                    "${email}.png"
        )

        gsRef.downloadUrl
            .addOnSuccessListener { urlImage ->
                Glide.with(applicationContext).load(urlImage).into(iv_profile_pic_chat)
            }

        //Display status
        var status: String = intent.extras?.get("status") as String
        tv_status.text = status

        if (status == "offline") {
            iv_status.setImageResource(R.drawable.ic_offline)

            firebaseDatabase.getReference("users")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnapShot in snapshot.children) {
                            val user: User? = postSnapShot.getValue(User::class.java)
                            if (user?.uid == receiverUID) {
                                if (user.lastOnline != "") tv_status.text =
                                    "Hoạt động lần cuối ${user.lastOnline}"
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

        } else if (status == "online") {
            iv_status.setImageResource(R.drawable.ic_online)
        }
    }

    fun handleEvent() {

        ivSend.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val msg: String = edtMsg.text.toString()

                if (msg.isNotEmpty()) {
                    val time: String = SimpleDateFormat("HH:mm").format(Date())
                    var currUser = FirebaseAuth.getInstance().currentUser
                    if (currUser != null) {
                        msgList.add(Message(msg, time, currUser.uid, receiverUID, "text", ""))
                        msgAdapter.notifyItemInserted(msgList.size - 1)
                        recyclerView.scrollToPosition(msgList.size - 1)
                        val msgObj = Message(msg, time, currUser.uid, receiverUID, "text", "")

                        dbRef.child("messages").child(senderRoom).push()
                            .setValue(msgObj).addOnSuccessListener {
                                dbRef.child("messages").child(receiverRoom).push()
                                    .setValue(msgObj)
                            }

                        // ???????????????????????
//                        Collections.swap(userList, currPos, 0)
//                        UserListActivity.userAdapter.notifyItemMoved(currPos, 0)

                        val map: HashMap<String, Map<String, String>> = HashMap()
                        map.put("timestamp", ServerValue.TIMESTAMP)

                        dbRef.child("users").child(senderUID)
                            .updateChildren(map as Map<String, Any>)
                        dbRef.child("users").child(receiverUID)
                            .updateChildren(map as Map<String, Any>)


                    }

                }
                edtMsg.setText("")

            }

        })

        iv_pickImg.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                ImagePicker.with(this@ChatActivity)
                    .start()
            }

        })

        iv_back.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                Log.d("destroy", "destroy")
                onBackPressed()
            }

        })

        rcv_msg.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                edtMsg.clearFocus()
                rcv_msg.requestFocus()
                hideKeyBoard()
                return false
            }

        })

        ln_chat.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                edtMsg.clearFocus()
                ln_chat.requestFocus()
                hideKeyBoard()
                return false
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
                            Log.d("size",msgList.size.toString())
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

    fun getRecyclerView() = this.recyclerView

    fun getEdtMsg() = this.edtMsg

    fun getScreenWidth(): Int {
        var displayMetrics: DisplayMetrics = resources.displayMetrics

        return displayMetrics.widthPixels
    }

    fun hideKeyBoard() {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    fun addUsertoDB(user: User) {
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
        dbRef.setValue(user)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            var uri: Uri? = data?.data


            val storage = Firebase.storage("gs://feisty-flow-326908.appspot.com")

            val storageRef = storage.reference

            val currTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())


            val riversRef1 = storageRef.child(
                "$senderUID$receiverUID/$currTime"
            )

            val uploadTask = uri?.let { riversRef1.putFile(it) }


// Register observers to listen for when the download is done or if it fails
            uploadTask?.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("upload", "unsuccessfull")
            }?.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
            }

            val riversRef2 = storageRef.child(
                "$receiverUID$senderUID/$currTime"
            )

            val uploadTask2 = uri?.let { riversRef2.putFile(it) }

            uploadTask2?.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("upload", "unsuccessfull")
            }?.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
            }

            val downloadUrl =
                "gs://feisty-flow-326908.appspot.com/${senderUID}${receiverUID}/$currTime"

            val msg = Message(
                "",
                SimpleDateFormat("HH:mm").format(Date()),
                senderUID,
                receiverUID,
                "image",
                downloadUrl
            )


            msgList.add(msg)

            msgAdapter.notifyItemInserted(msgList.size - 1)
            recyclerView.scrollToPosition(msgList.size - 1)

            dbRef.child("messages").child(senderRoom).push()
                .setValue(msg).addOnSuccessListener {
                    dbRef.child("messages").child(receiverRoom).push()
                        .setValue(msg)
                }

            val map: HashMap<String, Map<String, String>> = HashMap()
            map.put("timestamp", ServerValue.TIMESTAMP)

            dbRef.child("users").child(senderUID)
                .updateChildren(map as Map<String, Any>)
            dbRef.child("users").child(receiverUID)
                .updateChildren(map as Map<String, Any>)
//        msgAdapter.notifyItemInserted(msgList.size - 1)
//        recyclerView.scrollToPosition(msgList.size - 1)
        }

    }

    override fun onResume() {
        checkInChat = true
        super.onResume()
    }

//    override fun onStop() {
//        Log.d("Vit", "chat activity Stop")
//        checkInChat = false;
//        super.onStop()
//    }

    override fun onDestroy() {

        Log.d("Vit", "chat activity Destroy")
        checkInChat = false
        super.onDestroy()
    }

    companion object {
        var checkInChat = false
    }
}


