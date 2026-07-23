package com.hani.assistant.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface PreferenceDao {

    @Query("SELECT * FROM preferences LIMIT 1")
    suspend fun getPreferences(): UserPreferenceEntity?

    @Update
    suspend fun updatePreferences(preferences: UserPreferenceEntity)

    @Query("UPDATE preferences SET userName = :name WHERE id = 1")
    suspend fun updateUserName(name: String)
}
