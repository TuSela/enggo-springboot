package com.example.appenggo.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.data.AppDatabase
import com.example.appenggo.data.NotificationMessageEntity
import kotlin.concurrent.thread
import com.example.appenggo.adapter.NotificationAdapter
import com.example.appenggo.websocket.WebSocketManager

/**
 * Activity displaying a list of stored notification messages.
 * For PVP invite notifications it shows Accept / Decline buttons.
 */
class NotificationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_list)

        recyclerView = findViewById(R.id.recycler_notifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter { notification, action ->
            when (action) {
                NotificationAdapter.Action.ACCEPT -> handlePvpResponse(notification, true)
                NotificationAdapter.Action.DECLINE -> handlePvpResponse(notification, false)
                else -> {}
            }
        }
        recyclerView.adapter = adapter

        db = AppDatabase.getInstance(this)
        loadMessages()
    }

    private fun loadMessages() {
        thread {
            val messages = db.notificationDao().getAllMessages()
            runOnUiThread { adapter.submitList(messages) }
        }
    }

    private fun handlePvpResponse(notification: NotificationMessageEntity, accept: Boolean) {
        // Expect the requestId field in the original notification payload (if any)
        val requestId = notification.requestId ?: run {
            Toast.makeText(this, "Missing requestId", Toast.LENGTH_SHORT).show()
            return
        }
        // Build JSON payload to send back via WebSocketManager
        val payload = "{" +
                "\"requestId\":$requestId," +
                "\"accept\":$accept}";
        // Send via existing WebSocketManager (network on background thread)
        thread {
            WebSocketManager.respondInvite(payload)
        }
        // Remove the notification from local DB after response
        thread {
            db.notificationDao().deleteMessage(notification)
        }
        Toast.makeText(this, if (accept) "Đã chấp nhận lời mời" else "Đã từ chối lời mời", Toast.LENGTH_SHORT).show()
    }
}
