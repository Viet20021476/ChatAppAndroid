package adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chattingapp.ChatActivity
import com.example.chattingapp.LoginActivity
import com.example.chattingapp.R
import com.example.chattingapp.UserListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_userlist.*
import kotlinx.android.synthetic.main.item_user.view.*
import model.Message
import model.User
import org.w3c.dom.Text

class UserAdapter(private var userList: MutableList<User>, private var activity: UserListActivity) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var context: Context = activity.applicationContext
        var layoutInflater: LayoutInflater = LayoutInflater.from(context)

        var userView: View = layoutInflater.inflate(R.layout.item_user, parent, false)

        return UserViewHolder(userView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        var user: User = userList[position]

        holder.userName.text = user.name
        holder.lastmsg.text = ""

        if (user.status == "online") {
            holder.itemView.iv_status_item.setImageResource(R.drawable.ic_online)
        } else {
            holder.itemView.iv_status_item.setImageResource(R.drawable.ic_offline)
        }

        displayLastMSG(
            FirebaseAuth.getInstance().currentUser!!.uid,
            user.uid,
            holder.lastmsg,
        )

        val storage = Firebase.storage

        val storageRef = storage.reference

        val gsRef = storage.getReferenceFromUrl(
            "gs://feisty-flow-326908.appspot.com/image" +
                    "${user.email}.png"
        )

        gsRef.downloadUrl
            .addOnSuccessListener { urlImage ->
                Glide.with(activity).load(urlImage).into(holder.userPic)
            }

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                var intent: Intent = Intent(activity, ChatActivity::class.java)

                var bundle: Bundle = Bundle()

                bundle.putString("name", user.name)
                bundle.putString("uid", user.uid)
                bundle.putString("email", user.email)
                bundle.putString("status", user.status)
                bundle.putInt("pos", holder.adapterPosition)
                bundle.putParcelableArrayList("user_list", ArrayList(userList))
                intent.putExtras(bundle)

                activity.startActivity(intent)
            }

        })


    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun displayLastMSG(
        senderId: String,
        receiverId: String,
        lastMsg: TextView,
    ) {
        val currUser = FirebaseAuth.getInstance().currentUser
        val dbRef =
            FirebaseDatabase.getInstance().getReference("messages").child(senderId + receiverId)
        Log.d("Vitt", senderId + receiverId)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val msg = postSnapshot.getValue(Message::class.java)

                    if (msg != null) Log.d("Vitt", msg.getSenderId())

                    if ((msg?.getSenderId().equals(senderId) && (msg?.getReceiverId()
                            .equals(receiverId))) || ((msg?.getSenderId()
                            .equals(receiverId) && (msg?.getReceiverId()
                            .equals(senderId))))
                    ) {
                        Log.d("Vitt", "INNNNNNNNNNNNNNNN")
                        lastMsg.text = msg?.getText() + " " + msg?.getTime()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView
        var lastmsg: TextView
        var userPic: ImageView

        init {
            userName = itemView.tv_username
            lastmsg = itemView.tv_lastmsg
            userPic = itemView.iv_profile_pic
        }


    }


}