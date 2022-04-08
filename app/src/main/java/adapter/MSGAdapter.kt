package adapter

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chattingapp.ChatActivity
import com.example.chattingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import model.Message


class MSGAdapter(
    private var chatActivity: ChatActivity,
    private var msgList: MutableList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var context: Context = chatActivity.applicationContext
        var layoutInflater: LayoutInflater = LayoutInflater.from(context)
        var msgView: View = layoutInflater.inflate(R.layout.item_msg_receive, parent, false)

        if (viewType == ITEM_SENT) {

            msgView = layoutInflater.inflate(R.layout.item_msg_send, parent, false)
            Log.d("Check", "Send")

            return SendMSGViewHolder(msgView)
        } else if (viewType == ITEM_RECEIVE) {
            msgView = layoutInflater.inflate(R.layout.item_msg_receive, parent, false)


            return ReceiveMSGViewHolder(msgView)
        }
        return ReceiveMSGViewHolder(msgView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var msg: Message = msgList[position]
        Log.d("checkdl", msg.getDownloadUri())


        if (holder.javaClass == SendMSGViewHolder::class.java) {
            val viewHolder = holder as SendMSGViewHolder
            if (msg.getType() == "text") {
                viewHolder.ivImageMsg.visibility = View.GONE
                viewHolder.getTVMSG().text = msg.getText()
                viewHolder.getTVTime().text = msg.getTime()
            } else {
                viewHolder.getTVMSG().visibility = View.GONE

                val storage = Firebase.storage

                val storageRef = storage.reference

                val gsRef = storage.getReferenceFromUrl(msg.getDownloadUri())

                Log.d("name", gsRef.name)

                val handler = Handler()

                Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                    override fun run() {
                        gsRef.downloadUrl
                            .addOnSuccessListener { urlImage ->
                                Glide.with(chatActivity).load(urlImage).into(viewHolder.ivImageMsg)
                            }
                    }
                }, 1000)


            }

            viewHolder.clMsgSend.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    chatActivity.getEdtMsg().clearFocus()
                    chatActivity.getRecyclerView().requestFocus()

                    val imm: InputMethodManager =
                        chatActivity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (chatActivity.currentFocus != null) {
                        imm.hideSoftInputFromWindow(chatActivity.currentFocus!!.windowToken, 0)
                    }
                }
            })


        } else if (holder.javaClass == ReceiveMSGViewHolder::class.java) {
            val viewHolder = holder as ReceiveMSGViewHolder

            if (msg.getType() == "text") {
                viewHolder.getTVMSGR().text = msg.getText()
                viewHolder.getTVTimeR().text = msg.getTime()
                viewHolder.ivImageMsgR.isVisible = false
            } else {
                viewHolder.getTVMSGR().isVisible = false

                val storage = Firebase.storage

                val storageRef = storage.reference

                if (msg.getDownloadUri() != "") {

                    val gsRef = storage.getReferenceFromUrl(msg.getDownloadUri())

                    Log.d("url", msg.getDownloadUri())

                    Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                        override fun run() {
                            gsRef.downloadUrl
                                .addOnSuccessListener { urlImage ->
                                    Glide.with(chatActivity).load(urlImage)
                                        .into(viewHolder.ivImageMsgR)
                                }
                        }
                    }, 1000)


                }
            }

            viewHolder.clMsgReceive.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    chatActivity.getEdtMsg().clearFocus()
                    chatActivity.getRecyclerView().requestFocus()

                    val imm: InputMethodManager =
                        chatActivity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (chatActivity.currentFocus != null) {
                        imm.hideSoftInputFromWindow(chatActivity.currentFocus!!.windowToken, 0)
                    }
                }
            })

            if (isValidContextForGlide(chatActivity.applicationContext)) {
                chatActivity.gsRef.downloadUrl
                    .addOnSuccessListener { urlImage ->
                        Glide.with(chatActivity.applicationContext).load(urlImage)
                            .into(viewHolder.ivReceiverPic)
                    }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        val currMSG = msgList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currMSG.getSenderId())) {
            return ITEM_SENT
        } else {
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    inner class SendMSGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvMsg: TextView
        private var tvTime: TextView
        var ivImageMsg: AppCompatImageView
        var clMsgSend: View

        init {
            tvMsg = itemView.findViewById(R.id.tv_msg)
            tvTime = itemView.findViewById(R.id.tv_time)
            ivImageMsg = itemView.findViewById(R.id.iv_imgMsg)
            clMsgSend = itemView.findViewById(R.id.cl_msg_send)

            tvMsg.maxWidth = (chatActivity.getScreenWidth() / 2 + 50)
        }

        fun getTVMSG() = this.tvMsg
        fun getTVTime() = this.tvTime

    }

    inner class ReceiveMSGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvMsgR: TextView
        private var tvTimeR: TextView
        var ivReceiverPic: CircleImageView
        var ivImageMsgR: AppCompatImageView
        var clMsgReceive: View


        init {
            tvMsgR = itemView.findViewById(R.id.tv_msg_receive)
            tvTimeR = itemView.findViewById(R.id.tv_time_receive)
            ivReceiverPic = itemView.findViewById(R.id.iv_receiver_pic)
            clMsgReceive = itemView.findViewById(R.id.cl_msg_receive)
            ivImageMsgR = itemView.findViewById(R.id.iv_imgMsgR)
            tvMsgR.maxWidth = (chatActivity.getScreenWidth() / 2 + 50)
        }

        fun getTVMSGR() = this.tvMsgR
        fun getTVTimeR() = this.tvTimeR

    }

    companion object {
        const val ITEM_SENT = 2
        const val ITEM_RECEIVE = 1
    }

    fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            val activity = context
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }


}

