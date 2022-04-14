package model

import com.google.firebase.auth.FirebaseAuth

class Message {
    private var id: String = ""
    private var text: String = ""
    private var time: String = ""
    private var senderId: String = ""
    private var receiverId: String = ""
    private var type: String = ""
    private var downloadUrl = ""
    var isSeen: Boolean = false

    constructor() {
//        this.senderId = FirebaseAuth.getInstance().currentUser!!.uid // Problem
    }

    constructor(id: String, senderId: String, receiverId: String) {
        this.id = id
        this.senderId = senderId
        this.receiverId = receiverId
    }

    constructor(
        id: String,
        text: String,
        time: String,
        senderId: String,
        receiverId: String,
        type: String,
        downloadUrl: String,
        isSeen: Boolean
    ) {
        this.id = id
        this.text = text
        this.time = time
        this.senderId = senderId
        this.receiverId = receiverId
        this.type = type
        this.downloadUrl = downloadUrl
        this.isSeen = isSeen
    }

    fun getId() = this.id
    fun getText() = this.text
    fun getTime() = this.time
    fun getSenderId() = this.senderId
    fun getReceiverId() = this.receiverId
    fun getType() = this.type
    fun getDownloadUrl() = this.downloadUrl

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (senderId != other.senderId) return false
        if (receiverId != other.receiverId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + senderId.hashCode()
        result = 31 * result + receiverId.hashCode()
        return result
    }


}