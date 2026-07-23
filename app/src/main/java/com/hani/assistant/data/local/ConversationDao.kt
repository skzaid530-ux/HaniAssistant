package com.hani.assistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ConversationDao {

    @Insert
    suspend fun insert(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
