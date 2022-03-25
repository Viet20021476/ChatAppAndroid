package adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.ChatActivity
import com.example.chattingapp.R
import com.google.firebase.auth.FirebaseAuth
import model.Message

class MSGAdapter(
    private var mainActivity: ChatActivity,
    private var msgList: MutableList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var context: Context = mainActivity.applicationContext
        var layoutInflater: LayoutInflater = LayoutInflater.from(context)
        var msgView: View = layoutInflater.inflate(R.layout.item_msg_receive, parent, false)

        if (viewType == ITEM_SENT) {

            msgView = layoutInflater.inflate(R.layout.item_msg_send, parent, false)
            Log.d("Check","Send")

            return SendMSGViewHolder(msgView)
        } else if (viewType == ITEM_RECEIVE) {
            msgView = layoutInflater.inflate(R.layout.item_msg_receive, parent, false)


            return ReceiveMSGViewHolder(msgView)
        }
        return ReceiveMSGViewHolder(msgView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var msg: Message = msgList[position]


        if (holder.javaClass == SendMSGViewHolder::class.java) {
            val viewHolder = holder as SendMSGViewHolder
            viewHolder.getTVMSG().text = msg.getText()
            viewHolder.getTVTime().text = msg.getTime()

        } else if (holder.javaClass == ReceiveMSGViewHolder::class.java) {
            val viewHolder = holder as ReceiveMSGViewHolder
            viewHolder.getTVMSG().text = msg.getText()
            viewHolder.getTVTime().text = msg.getTime()
        }

    }

    override fun getItemViewType(position: Int): Int {
        val currMSG = msgList[position]

        Log.d("test",FirebaseAuth.getInstance().currentUser!!.uid)
        Log.d("test",currMSG.getSenderId())

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currMSG.getSenderId())) {
            return ITEM_SENT
        } else {
            return ITEM_RECEIVE
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    inner class SendMSGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvMsg: TextView
        private var tvTime: TextView

        init {
            tvMsg = itemView.findViewById(R.id.tv_msg)
            tvTime = itemView.findViewById(R.id.tv_time)
            tvMsg.maxWidth = (mainActivity.getScreenWidth() / 2 + 50).toInt()
        }

        fun getTVMSG() = this.tvMsg
        fun getTVTime() = this.tvTime

    }

    inner class ReceiveMSGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvMsgR: TextView
        private var tvTimeR: TextView

        init {
            tvMsgR = itemView.findViewById(R.id.tv_msg_receive)
            tvTimeR = itemView.findViewById(R.id.tv_time_receive)
            tvMsgR.maxWidth = (mainActivity.getScreenWidth() / 2 + 50).toInt()
        }

        fun getTVMSG() = this.tvMsgR
        fun getTVTime() = this.tvTimeR

    }

    companion object {
        const val ITEM_SENT = 2
        const val ITEM_RECEIVE = 1
    }


}

