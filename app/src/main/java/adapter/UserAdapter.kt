package adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.ChatActivity
import com.example.chattingapp.LoginActivity
import com.example.chattingapp.R
import com.example.chattingapp.UserListActivity
import kotlinx.android.synthetic.main.item_user.view.*
import model.User

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

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                var intent: Intent = Intent(activity, ChatActivity::class.java)

                var bundle: Bundle = Bundle()

                bundle.putString("name", user.name)
                bundle.putString("uid", user.uid)
                intent.putExtras(bundle)

                activity.startActivity(intent)
            }

        })

    }

    override fun getItemCount(): Int {
        return userList.size
    }


    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView

        init {
            userName = itemView.tv_username
        }


    }


}