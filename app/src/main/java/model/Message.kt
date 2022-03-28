package model

import com.google.firebase.auth.FirebaseAuth

class Message {
    private var text: String = ""
    private var time: String = ""
    private var senderId: String = ""
    private var receiverId: String = ""
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    constructor() {
//        this.senderId = FirebaseAuth.getInstance().currentUser!!.uid // Problem
    }

    constructor(text: String, time: String, senderId: String, receiverId: String) {
        this.text = text;
        this.time = time;
        this.senderId = senderId
        this.receiverId = receiverId
    }


    fun getText() = this.text
    fun getTime() = this.time
    fun getSenderId() = this.senderId
    fun getReceiverId() = this.receiverId

    fun setText(text: String) {
        this.text = text
    }

    fun setTime(time: String) {
        this.time = time;
    }

}