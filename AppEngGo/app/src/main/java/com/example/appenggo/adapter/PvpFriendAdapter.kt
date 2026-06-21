package com.example.appenggo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appenggo.R
import com.example.appenggo.model.FriendResponse

class PvpFriendAdapter(
    private val items: MutableList<FriendResponse> = mutableListOf(),
    private val onInviteClick: (FriendResponse) -> Unit
) : RecyclerView.Adapter<PvpFriendAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val dotOnline: View = view.findViewById(R.id.dot_online)
        val btnInvite: Button = view.findViewById(R.id.btn_invite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pvp_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = items[position]
        holder.tvUsername.text = friend.username

        if (friend.online) {
            holder.dotOnline.visibility = View.VISIBLE
            holder.tvStatus.text = "Đang hoạt động"
            holder.tvStatus.setTextColor(Color.parseColor("#78C800"))
        } else {
            holder.dotOnline.visibility = View.GONE
            holder.tvStatus.text = "Ngoại tuyến"
            holder.tvStatus.setTextColor(Color.parseColor("#AFAFAF"))
        }

        if (!friend.avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(friend.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        holder.btnInvite.setOnClickListener { onInviteClick(friend) }
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<FriendResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateOnlineStatus(userId: Int, isOnline: Boolean) {
        val index = items.indexOfFirst { it.userId == userId }
        if (index != -1) {
            items[index] = items[index].copy(online = isOnline)
            notifyItemChanged(index)
        }
    }
}
