package com.hani.assistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class UserPreferenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1, // single row
    val userName: String? = null,
    val preferredVoicePitch: Float = 1.0f,
    val preferredSpeed: Float = 1.0f,
    val wakeWord: String = "Hani",
    val memoryEnabled: Boolean = true,
    val recentApps: List<String> = emptyList(),
    val favoriteContacts: List<String> = emptyList()
)