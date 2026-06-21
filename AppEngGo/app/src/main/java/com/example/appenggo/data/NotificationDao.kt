package com.example.appenggo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

/**
 * DAO for accessing the notification count stored in the local SQLite database.
 */
@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification WHERE id = 1")
    fun getNotification(): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: NotificationEntity)

    @Update
    fun update(notification: NotificationEntity)

    // Insert a notification message
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: NotificationMessageEntity)

    // Retrieve all stored notification messages, newest first
    @Query("SELECT * FROM notification_message ORDER BY timestamp DESC")
    fun getAllMessages(): List<NotificationMessageEntity>

    // Delete a specific message (used after handling invite)
    @Delete
    fun deleteMessage(message: NotificationMessageEntity)

    /**
     * Utility method to set the unread count. If the row does not exist we insert a new one.
     */
    fun setUnreadCount(count: Int) {
        val existing = getNotification()
        if (existing == null) {
            insert(NotificationEntity(unreadCount = count))
        } else {
            update(existing.copy(unreadCount = count))
        }
    }
}
