package com.example.appenggo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.data.NotificationMessageEntity

/**
 * RecyclerView adapter for showing notification messages.
 * For PVP invites it displays Accept / Decline buttons.
 */
class NotificationAdapter(
    private val onAction: (NotificationMessageEntity, Action) -> Unit
) : ListAdapter<NotificationMessageEntity, NotificationAdapter.ViewHolder>(DiffCallback()) {

    enum class Action { ACCEPT, DECLINE, NONE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)
        private val tvFrom: TextView = itemView.findViewById(R.id.tv_from)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val btnAccept: Button = itemView.findViewById(R.id.btn_accept)
        private val btnDecline: Button = itemView.findViewById(R.id.btn_decline)

        fun bind(notification: NotificationMessageEntity) {
            tvType.text = notification.type
            tvFrom.text = notification.fromUsername ?: ""
            tvMessage.text = notification.message

            // Show action buttons only for PVP invites
            if (notification.type == "PVP_INVITE") {
                btnAccept.visibility = View.VISIBLE
                btnDecline.visibility = View.VISIBLE
                btnAccept.setOnClickListener { onAction(notification, Action.ACCEPT) }
                btnDecline.setOnClickListener { onAction(notification, Action.DECLINE) }
            } else {
                btnAccept.visibility = View.GONE
                btnDecline.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationMessageEntity>() {
        override fun areItemsTheSame(oldItem: NotificationMessageEntity, newItem: NotificationMessageEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationMessageEntity, newItem: NotificationMessageEntity): Boolean {
            return oldItem == newItem
        }
    }
}
