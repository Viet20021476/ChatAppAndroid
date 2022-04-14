package adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chattingapp.ChatActivity
import com.example.chattingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*
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

        val storage = Firebase.storage

        val storageRef = storage.reference

        var msg: Message = msgList[position]

        if (holder.javaClass == SendMSGViewHolder::class.java) {
            val viewHolder = holder as SendMSGViewHolder
            if (msg.getType() == "text") {
                viewHolder.ivImageMsg.visibility = View.GONE
                viewHolder.getTVMSG().visibility = View.VISIBLE
                viewHolder.getTVTime().visibility = View.VISIBLE
                viewHolder.getTVMSG().text = msg.getText()
                viewHolder.getTVTime().text = msg.getTime()

//                if (position == msgList.size - 1) {
//                    if (msg.isSeen) {
//
//                        val drawable = chatActivity.iv_profile_pic_chat.drawable
//                        if (drawable == null) Log.d("checknull", "NULLLLLLLLL")
//                        viewHolder.ivSeen.setImageDrawable(R.drawable.chi.toDrawable())
//                    }
//                }

                if (position == msgList.size - 1) {
                    if (msg.isSeen) {
                        viewHolder.ivSeen.visibility = View.VISIBLE
                        val drawable = chatActivity.iv_profile_pic_chat.drawable
                        viewHolder.ivSeen.setImageDrawable(drawable)
                    } else {
                        viewHolder.ivSeen.visibility = View.GONE
                    }
                } else if (position == msgList.size - 2 && !msgList[msgList.size - 1].isSeen) {
                    if (msg.isSeen) {
                        viewHolder.ivSeen.visibility = View.VISIBLE
                        val drawable = chatActivity.iv_profile_pic_chat.drawable
                        viewHolder.ivSeen.setImageDrawable(drawable)
                    } else {
                        viewHolder.ivSeen.visibility = View.GONE
                    }
                } else {
                    viewHolder.ivSeen.visibility = View.GONE
                }

            } else {
                viewHolder.getTVMSG().visibility = View.GONE
                viewHolder.getTVTime().visibility = View.VISIBLE
                viewHolder.ivImageMsg.visibility = View.VISIBLE
                viewHolder.getTVTime().text = msg.getTime()

                val constraintSet = ConstraintSet()
                constraintSet.clone(viewHolder.clMsgSend_layout)
                constraintSet.connect(
                    viewHolder.getTVTime().id,
                    ConstraintSet.TOP,
                    viewHolder.ivImageMsg.id,
                    ConstraintSet.BOTTOM,
                    0
                )
                constraintSet.applyTo(viewHolder.clMsgSend_layout)

                val gsRef = storage.getReferenceFromUrl(msg.getDownloadUrl())

                gsRef.downloadUrl
                    .addOnSuccessListener { urlImage ->
                        Glide.with(chatActivity).load(urlImage).into(viewHolder.ivImageMsg)
                    }

                if (position == msgList.size - 1) {
                    if (msg.isSeen) {
                        viewHolder.ivSeen.visibility = View.VISIBLE
                        val drawable = chatActivity.iv_profile_pic_chat.drawable
                        viewHolder.ivSeen.setImageDrawable(drawable)
                    } else {
                        viewHolder.ivSeen.visibility = View.GONE
                    }
                } else if (position == msgList.size - 2 && !msgList[msgList.size - 1].isSeen) {
                    if (msg.isSeen) {
                        viewHolder.ivSeen.visibility = View.VISIBLE
                        val drawable = chatActivity.iv_profile_pic_chat.drawable
                        viewHolder.ivSeen.setImageDrawable(drawable)
                    } else {
                        viewHolder.ivSeen.visibility = View.GONE
                    }
                } else {
                    viewHolder.ivSeen.visibility = View.GONE
                }
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
                viewHolder.getTVMSGR().visibility = View.VISIBLE
                viewHolder.getTVTimeR().visibility = View.VISIBLE
                viewHolder.ivImageMsgR.isVisible = false
            } else {
                viewHolder.getTVMSGR().visibility = View.GONE
                viewHolder.getTVTimeR().visibility = View.VISIBLE
                viewHolder.ivImageMsgR.visibility = View.VISIBLE
                viewHolder.getTVTimeR().text = msg.getTime()

                val constraintSet = ConstraintSet()
                constraintSet.clone(viewHolder.clMsgReceive_layout)
                constraintSet.connect(
                    viewHolder.getTVTimeR().id,
                    ConstraintSet.TOP,
                    viewHolder.ivImageMsgR.id,
                    ConstraintSet.BOTTOM,
                    0
                )
                constraintSet.applyTo(viewHolder.clMsgReceive_layout)


                val storage = Firebase.storage

                val storageRef = storage.reference

                if (msg.getDownloadUrl() != "") {

                    val gsRef = storage.getReferenceFromUrl(msg.getDownloadUrl())

                    gsRef.downloadUrl
                        .addOnSuccessListener { urlImage ->
                            Glide.with(chatActivity).load(urlImage)
                                .into(viewHolder.ivImageMsgR)
                        }

//                    Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
//                        override fun run() {
//
//                        }
//                    }, 1000)


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
        var clMsgSend_layout: ConstraintLayout
        var ivSeen: CircleImageView

        init {
            tvMsg = itemView.findViewById(R.id.tv_msg)
            tvTime = itemView.findViewById(R.id.tv_time)
            ivImageMsg = itemView.findViewById(R.id.iv_imgMsg)
            clMsgSend = itemView.findViewById(R.id.cl_msg_send)
            clMsgSend_layout = itemView.findViewById(R.id.cl_msg_send)
            ivSeen = itemView.findViewById(R.id.iv_seen)

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
        var clMsgReceive_layout: ConstraintLayout


        init {
            tvMsgR = itemView.findViewById(R.id.tv_msg_receive)
            tvTimeR = itemView.findViewById(R.id.tv_time_receive)
            ivReceiverPic = itemView.findViewById(R.id.iv_receiver_pic)
            clMsgReceive = itemView.findViewById(R.id.cl_msg_receive)
            ivImageMsgR = itemView.findViewById(R.id.iv_imgMsgR)
            clMsgReceive_layout = itemView.findViewById(R.id.cl_msg_receive)
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

