package com.example.appenggo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.adapter.PvpFriendAdapter
import com.example.appenggo.websocket.WebSocketManager
import kotlinx.coroutines.launch

class PvpActivity : AppCompatActivity() {

    private lateinit var tabRanking: TextView
    private lateinit var tabInvite: TextView
    private lateinit var layoutRankingContent: ScrollView
    private lateinit var layoutInviteContent: ScrollView

    private lateinit var cardTopic: LinearLayout
    private lateinit var tvTopicValue: TextView

    private lateinit var cardDifficulty: LinearLayout
    private lateinit var cardQuestionCount: LinearLayout
    private lateinit var tvQuestionCountValue: TextView
    private lateinit var tvDifficultyValue: TextView
    private lateinit var ivDifficultyIcon: ImageView

    private lateinit var rvPvpFriends: RecyclerView
    private lateinit var pvpFriendAdapter: PvpFriendAdapter

    private var currentTopicId: Int = 1
    private var currentTopicName: String = "Gia đình"
    private var currentDifficulty: DifficultyBottomSheet.Difficulty = DifficultyBottomSheet.Difficulty.MEDIUM
    private var currentQuestionCount: Int = 10

    private val topicLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val themeId = result.data?.getIntExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, -1) ?: -1
            val themeName = result.data?.getStringExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_NAME)
            if (themeId != -1 && themeName != null) {
                currentTopicId = themeId
                currentTopicName = themeName
                tvTopicValue.text = themeName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp)

        initViews()
        setupRecyclerView()
        setupClickListeners()

        tvTopicValue.text = currentTopicName
        tvDifficultyValue.text = currentDifficulty.label
        updateSettingCardSelection(cardDifficulty)

        loadFriends()
        listenOnlineStatus()
    }

    private fun initViews() {
        tabRanking           = findViewById(R.id.tab_ranking)
        tabInvite            = findViewById(R.id.tab_invite)
        layoutRankingContent = findViewById(R.id.layout_ranking_content)
        layoutInviteContent  = findViewById(R.id.layout_invite_content)
        cardTopic            = findViewById(R.id.card_topic)
        tvTopicValue         = findViewById(R.id.tv_topic_value)
        cardDifficulty       = findViewById(R.id.card_difficulty)
        tvDifficultyValue    = findViewById(R.id.tv_difficulty_value)
        ivDifficultyIcon      = findViewById(R.id.iv_difficulty_icon)
        cardQuestionCount     = findViewById(R.id.card_question_count)
        tvQuestionCountValue  = findViewById(R.id.tv_question_count_value)
        rvPvpFriends         = findViewById(R.id.rv_pvp_friends)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        pvpFriendAdapter = PvpFriendAdapter { friend ->
            // Khi người dùng chọn "Mời" một bạn bè, gửi lời mời PvP qua WebSocket
            sendPvPInvite(friend)
        }
        rvPvpFriends.layoutManager = LinearLayoutManager(this)
        rvPvpFriends.adapter = pvpFriendAdapter
    }

    /**
     * Gửi lời mời PvP tới một người bạn.
     * Dữ liệu mời bao gồm username của người nhận và các thông số quiz ngẫu nhiên.
     */
    private fun sendPvPInvite(friend: com.example.appenggo.model.FriendResponse) {
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("TOKEN", null) ?: return
        // Xây dựng payload InviteRequest
        val blueprint = com.example.appenggo.model.RandomBlueprintRequest(
            difficulty = when (currentDifficulty) {
                DifficultyBottomSheet.Difficulty.EASY -> 1
                DifficultyBottomSheet.Difficulty.MEDIUM -> 2
                DifficultyBottomSheet.Difficulty.HARD -> 3
            }.toByte(),
            themeIds = listOf(currentTopicId),
            totalQuestions = currentQuestionCount,
            // questionTypes có giá trị mặc định trong data class
        )
        val inviteReq = com.example.appenggo.model.InviteRequest(
            inviteeUsername = friend.username,
            randomBlueprintRequest = blueprint
        )
        // Chuyển đối thành JSON (Gson)
        val json = com.google.gson.Gson().toJson(inviteReq)
        com.example.appenggo.websocket.WebSocketManager.sendInvite("Bearer $token $json")
        Toast.makeText(this, "Đã gửi lời mời tới ${friend.username}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Xử lý khi nhận được lời mời PvP (được gửi tới người dùng hiện tại).
     * Hiển thị một dialog cho phép chấp nhận hoặc từ chối.
     */
    private fun handleInviteReceived(invite: com.example.appenggo.model.InviteResponse) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Lời mời PvP")
            .setMessage("${invite.inviterUsername} muốn mời bạn chơi PvP. Chấp nhận?")
            .setPositiveButton("Chấp nhận") { _, _ ->
                respondToInvite(invite.inviteId, true)
            }
            .setNegativeButton("Từ chối") { _, _ ->
                respondToInvite(invite.inviteId, false)
            }
            .setCancelable(false)
            .show()
    }

    private fun respondToInvite(inviteId: Int, accept: Boolean) {
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("TOKEN", null) ?: return
        val payload = mapOf("inviteId" to inviteId, "accepted" to accept)
        val json = com.google.gson.Gson().toJson(payload)
        com.example.appenggo.websocket.WebSocketManager.respondInvite("Bearer $token $json")
    }

    /**
     * Xử lý kết quả trả về của lời mời (ACCEPTED, DECLINED, TIMEOUT, ...).
     */
    private fun handleInviteResult(result: String) {
        // result là chuỗi đơn giản được server gửi (ví dụ: "INVITE_DECLINED" hoặc "INVITE_TIMEOUT")
        Toast.makeText(this, "Kết quả lời mời: $result", Toast.LENGTH_SHORT).show()
    }

    private fun loadFriends() {
        val token = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("TOKEN", null) ?: return

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getAllFriends("Bearer $token")
                if (res.code == 1000) {
                    pvpFriendAdapter.submitList(res.result ?: emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@PvpActivity, "Lỗi tải bạn bè", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenOnlineStatus() {
        WebSocketManager.onStatusChanged = { userId, status ->
            runOnUiThread {
                pvpFriendAdapter.updateOnlineStatus(userId, status == "ONLINE")
            }
        }
    }

    private fun setupClickListeners() {
        tabRanking.setOnClickListener { showRankingTab() }
        tabInvite.setOnClickListener  { showInviteTab() }
        cardTopic.setOnClickListener  { updateSettingCardSelection(cardTopic); openTopicSelection() }
        cardDifficulty.setOnClickListener { updateSettingCardSelection(cardDifficulty); openDifficultySelection() }
        cardQuestionCount.setOnClickListener { updateSettingCardSelection(cardQuestionCount); openQuestionCountSelection() }
    }

    private fun showRankingTab() {
        layoutRankingContent.visibility = View.VISIBLE
        layoutInviteContent.visibility  = View.GONE
        tabRanking.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabInvite.background = null
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    private fun showInviteTab() {
        layoutInviteContent.visibility  = View.VISIBLE
        layoutRankingContent.visibility = View.GONE
        tabInvite.background = ContextCompat.getDrawable(this, R.drawable.bg_pvp_tab_selected)
        tabInvite.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
        tabRanking.background = null
        tabRanking.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
    }

    private fun openTopicSelection() {
        val intent = Intent(this, ThemeSelectionActivity::class.java).apply {
            putExtra(ThemeSelectionActivity.EXTRA_SELECTED_THEME_ID, currentTopicId)
        }
        topicLauncher.launch(intent)
    }

    private fun openQuestionCountSelection() {
        val sheet = QuestionCountBottomSheet.newInstance(currentQuestionCount)
        sheet.onCountSelected = { count ->
            currentQuestionCount = count
            tvQuestionCountValue.text = "$count Câu"
        }
        sheet.show(supportFragmentManager, QuestionCountBottomSheet.TAG)
    }

    private fun updateSettingCardSelection(selected: LinearLayout) {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card_selected)
        val normalBg   = ContextCompat.getDrawable(this, R.drawable.bg_pvp_setting_card)
        cardTopic.background         = if (selected == cardTopic)         selectedBg else normalBg
        cardDifficulty.background    = if (selected == cardDifficulty)    selectedBg else normalBg
        cardQuestionCount.background = if (selected == cardQuestionCount) selectedBg else normalBg
    }

    private fun openDifficultySelection() {
        val bottomSheet = DifficultyBottomSheet.newInstance(currentDifficulty)
        bottomSheet.onDifficultySelected = { difficulty ->
            currentDifficulty = difficulty
            tvDifficultyValue.text = difficulty.label
            val iconRes = when (difficulty) {
                DifficultyBottomSheet.Difficulty.EASY   -> R.drawable.ic_easy
                DifficultyBottomSheet.Difficulty.MEDIUM -> R.drawable.ic_medium
                DifficultyBottomSheet.Difficulty.HARD   -> R.drawable.ic_hard
            }
            ivDifficultyIcon.setImageResource(iconRes)
        }
        bottomSheet.show(supportFragmentManager, DifficultyBottomSheet.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.onStatusChanged = null
    }
}
