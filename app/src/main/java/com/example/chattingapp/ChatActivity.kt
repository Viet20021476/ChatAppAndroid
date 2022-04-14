package com.example.chattingapp

import adapter.MSGAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_chat.*
import model.Message
import model.User
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {
    private lateinit var msgList: MutableList<Message>
    private lateinit var msgAdapter: MSGAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMsg: EditText
    private lateinit var ivSend: ImageView
    private lateinit var dbRef: DatabaseReference
    lateinit var gsRef: StorageReference
    lateinit var receiverUID: String
    lateinit var userList: MutableList<User>
    var currPos: Int = 0
    var senderUID = ""
    var senderRoom: String = ""
    var receiverRoom: String = ""
    lateinit var msgChildEventListener: ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        init()
        handleEvent()


    }

    fun init() {

        userList = intent.extras?.getParcelableArrayList("user_list")!!

        currPos = intent.extras?.get("pos") as Int

        val storage = Firebase.storage

        val storageRef = storage.reference

        val email: String = intent.extras?.get("email") as String

        gsRef = storage.getReferenceFromUrl(userList[currPos].profileImgUrl)

        Log.d("URL", userList[currPos].profileImgUrl.toString())

        gsRef.downloadUrl
            .addOnSuccessListener { urlImage ->
                Glide.with(this)
                    .asBitmap()
                    .load(urlImage)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            iv_profile_pic_chat.setImageBitmap(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })
            }





        edtMsg = findViewById(R.id.edt_msg)
        ivSend = findViewById(R.id.iv_send)
        msgList = mutableListOf()
        recyclerView = findViewById(R.id.rcv_msg)
        msgAdapter = MSGAdapter(this, msgList)
        var linearLayoutManager = LinearLayoutManager(this)

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


        //Load user profile pic

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


        recyclerView.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            recyclerView.scrollToPosition(msgList.size - 1)
        })

        ivSend.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val msg: String = edtMsg.text.toString()

                if (msg.isNotEmpty()) {
                    val time: String = SimpleDateFormat("HH:mm").format(Date())
                    var currUser = FirebaseAuth.getInstance().currentUser
                    if (currUser != null) {
//                        msgList.add(Message(msg, time, currUser.uid, receiverUID, "text", ""))
//                        msgAdapter.notifyItemInserted(msgList.size - 1)
                        val id: String =
                            dbRef.child("messages").child(senderRoom).push().key.toString()
                        recyclerView.scrollToPosition(msgList.size - 1)
                        val msgObj =
                            Message(id, msg, time, currUser.uid, receiverUID, "text", "", false)

                        dbRef.child("messages").child(senderRoom).child(id)
                            .setValue(msgObj).addOnSuccessListener {
                                dbRef.child("messages").child(receiverRoom).child(id)
                                    .setValue(msgObj)
                            }

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

        msgChildEventListener = dbRef.child("messages").child(senderRoom)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val temp_msg = snapshot.getValue(Message::class.java)
                    if (temp_msg != null) {
                        msgList.add(temp_msg)
                    }
                    var msg = Message(
                        temp_msg!!.getId(),
                        temp_msg.getSenderId(),
                        temp_msg.getReceiverId()
                    )

                    if (msg.getReceiverId() == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                        val pos = msgList.indexOf(msg)

                        msgList[pos].isSeen = true

                        msg.isSeen = true
                        val map: HashMap<String, Boolean> = HashMap()
                        map.put("seen", true)
                        dbRef.child("messages").child(senderRoom).child(msg.getId())
                            .updateChildren(
                                map as Map<String, Any>
                            )
                        dbRef.child("messages").child(receiverRoom).child(msg.getId())
                            .updateChildren(
                                map as Map<String, Any>
                            )

                    }

                    msgAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(msgList.size - 1)

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val temp_msg = snapshot.getValue(Message::class.java)
                    var msg = Message(
                        temp_msg!!.getId(),
                        temp_msg.getSenderId(),
                        temp_msg.getReceiverId()
                    )

                    if (msg.getSenderId() == FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                        val pos = msgList.indexOf(msg)
                        msgList[pos].isSeen = true
                        msgAdapter.notifyDataSetChanged()
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
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

            //val imgFile = File(uri?.path)


            val storage = Firebase.storage("gs://feisty-flow-326908.appspot.com")

            val storageRef = storage.reference

            val currTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())


            val riversRef1 = storageRef.child(
                "$senderUID$receiverUID/$currTime"
            )

            val uploadTask = uri?.let { riversRef1.putFile(it) }

            uploadTask?.addOnFailureListener {
                // Handle unsuccessful uploads
                Log.d("upload", "unsuccessfull")
            }?.addOnSuccessListener { taskSnapshot ->


                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                riversRef1.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    val id: String = dbRef.child("messages").child(senderRoom).push().key.toString()
                    val msg = Message(
                        id,
                        "",
                        SimpleDateFormat("HH:mm").format(Date()),
                        senderUID,
                        receiverUID,
                        "image",
                        downloadUrl,
                        true
                    )

                    dbRef.child("messages").child(senderRoom).child(id)
                        .setValue(msg).addOnSuccessListener {
                            dbRef.child("messages").child(receiverRoom).child(id)
                                .setValue(msg)

                            val map: HashMap<String, Map<String, String>> = HashMap()
                            map.put("timestamp", ServerValue.TIMESTAMP)

                            dbRef.child("users").child(senderUID)
                                .updateChildren(map as Map<String, Any>)
                            dbRef.child("users").child(receiverUID)
                                .updateChildren(map as Map<String, Any>)

                        }
                }
            }

        }

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        checkInChat = true
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        dbRef.child("messages").child(senderRoom).removeEventListener(msgChildEventListener)
    }

    override fun onDestroy() {

        Log.d("Vit", "chat activity Destroy")
        checkInChat = false
        super.onDestroy()
    }

    companion object {
        var checkInChat = false
    }

}


