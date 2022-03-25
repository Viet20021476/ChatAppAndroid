package model

import com.google.firebase.auth.FirebaseAuth

class Message {
    private var text: String = ""
    private var time: String = ""
    private var senderId: String = ""
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    constructor() {
//        this.senderId = FirebaseAuth.getInstance().currentUser!!.uid // Problem
    }

    constructor(text: String, time: String, senderId: String) {
        this.text = text;
        this.time = time;
        this.senderId = senderId
    }


    fun getText() = this.text
    fun getTime() = this.time
    fun getSenderId() = this.senderId

    fun setText(text: String) {
        this.text = text
    }

    fun setTime(time: String) {
        this.time = time;
    }

}