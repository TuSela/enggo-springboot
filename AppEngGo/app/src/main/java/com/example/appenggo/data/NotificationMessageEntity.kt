package com.example.appenggo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a single notification message stored locally.
 */
@Entity(tableName = "notification_message")
data class NotificationMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val fromUsername: String?,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: Int? = null
)
