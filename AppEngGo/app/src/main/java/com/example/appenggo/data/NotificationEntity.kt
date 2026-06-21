package com.example.appenggo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Simple entity to store the number of unread notifications.
 * We keep only a single row with a fixed id = 1.
 */
@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey val id: Int = 1,
    val unreadCount: Int = 0
)
