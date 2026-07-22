package com.aifront.data.local.dao

import androidx.room.*
import com.aifront.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE conversationType = :type ORDER BY updatedAt DESC")
    fun getConversationsByType(type: String): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): Conversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation): Long

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("UPDATE conversations SET messageCount = (SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId) WHERE id = :conversationId")
    suspend fun updateMessageCount(conversationId: Long)

    @Query("UPDATE conversations SET updatedAt = :updatedAt, messageCount = :messageCount WHERE id = :id")
    suspend fun updateConversationMeta(id: Long, updatedAt: Long, messageCount: Int)
}