package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.viewmodel.MainViewModel
import com.example.appenggo.websocket.WebSocketManager

class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private var tvStreak: TextView? = null
    private var tvLevel: TextView? = null
    private var tvProgress: TextView? = null
    private var btnLearnVocabulary: View? = null
    private var btn_battle: View? = null

    private var btnBell: View? = null
    private var tvNotificationBadge: TextView? = null
    private var unreadCount = 0 // will be persisted via Room

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        setupClickListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        observeViewModel()
        listenNotifications()
    }

    private fun initViews(view: View) {
        // Load unread count from Room
        val db = com.example.appenggo.data.AppDatabase.getInstance(requireContext())
        // This should be done off the main thread; using a simple thread for brevity
        Thread {
            val count = db.notificationDao().getNotification()?.unreadCount ?: 0
            unreadCount = count
            // Update UI on main thread
            activity?.runOnUiThread { updateBadge() }
        }.start()
        tvStreak = view.findViewById(R.id.tv_streak)
        tvLevel = view.findViewById(R.id.tv_level)
        tvProgress = view.findViewById(R.id.tv_progress)
        btnLearnVocabulary = view.findViewById(R.id.btn_learn_vocabulary)
        btn_battle = view.findViewById(R.id.btn_battle)
        btnBell = view.findViewById(R.id.btn_bell)
        tvNotificationBadge = view.findViewById(R.id.tv_notification_badge)
    }

    private fun setupClickListeners() {
        btnLearnVocabulary?.setOnClickListener {
            startActivity(Intent(requireContext(), VocabularyActivity::class.java))
        }
        btn_battle?.setOnClickListener {
            startActivity(Intent(requireContext(), PvpActivity::class.java))
        }

        // Click chuông → mở danh sách thông báo (reset badge)
        btnBell?.setOnClickListener {
            // Reset badge count
            unreadCount = 0
            updateBadge()
            // Open notification list activity
            val intent = Intent(requireContext(), NotificationListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun listenNotifications() {
        // Đăng ký callback nhận thông báo từ WebSocket
        WebSocketManager.onNotificationReceived = { notification ->
                // Existing notification handling (friend requests, etc.)
                //
                // Additionally handle PvP invites that come via the generic notification channel.
                // The payload already contains requestId, type, fromUsername, and message.
                // No extra logic needed here because the payload is saved below.
            }
            activity?.runOnUiThread {
                unreadCount++
                updateBadge()
                // Persist unread count and message to Room (off the UI thread)
                Thread {
                    val db = com.example.appenggo.data.AppDatabase.getInstance(requireContext())
                    db.notificationDao().setUnreadCount(unreadCount)
                    // Save the notification message
                    val messageEntity = com.example.appenggo.data.NotificationMessageEntity(
                        type = notification.type,
                        fromUsername = notification.fromUsername,
                        message = notification.message,
                        requestId = notification.requestId
                    )
                    db.notificationDao().insertMessage(messageEntity)
                }.start()

                // Hiện toast khi có thông báo mới
                val msg = when (notification.type) {
                    "FRIEND_REQUEST" -> "🔔 ${notification.fromUsername} gửi lời mời kết bạn"
                    "FRIEND_ACCEPTED" -> "✅ ${notification.fromUsername} đã chấp nhận kết bạn"
                    "PVP_INVITE" -> "⚔️ ${notification.fromUsername} gửi lời mời PvP"
                    else -> notification.message
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBadge() {
        if (unreadCount > 0) {
            tvNotificationBadge?.visibility = View.VISIBLE
            tvNotificationBadge?.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            tvNotificationBadge?.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            tvStreak?.text = " ${stats.streak}"
            tvLevel?.text = "LV. ${stats.level}"
            tvProgress?.text = "${stats.currentProgress}/${stats.totalProgress}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy callback khi fragment bị destroy tránh memory leak
        WebSocketManager.onNotificationReceived = null
    }
}