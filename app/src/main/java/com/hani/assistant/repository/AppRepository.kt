package com.hani.assistant.repository

import com.hani.assistant.data.local.AppDatabase
import com.hani.assistant.data.local.ConversationDao
import com.hani.assistant.data.local.ConversationEntity
import com.hani.assistant.data.local.PreferenceDao
import com.hani.assistant.data.local.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val db: AppDatabase,
    private val preferenceDao: PreferenceDao,
    private val conversationDao: ConversationDao
) {

    // Preferences
    suspend fun getUserPreferences(): UserPreferenceEntity? = preferenceDao.getPreferences()
    suspend fun updatePreferences(prefs: UserPreferenceEntity) = preferenceDao.updatePreferences(prefs)
    suspend fun updateUserName(name: String) = preferenceDao.updateUserName(name)

    // Conversations
    suspend fun saveConversation(userMsg: String, botMsg: String) {
        conversationDao.insert(ConversationEntity(
            timestamp = System.currentTimeMillis(),
            userMessage = userMsg,
            botResponse = botMsg
        ))
    }

    fun getRecentConversations(): Flow<List<ConversationEntity>> = conversationDao.getRecentConversations()

    suspend fun clearConversations() = conversationDao.clearAll()

    // Initial setup
    suspend fun initializePreferences() {
        if (preferenceDao.getPreferences() == null) {
            preferenceDao.updatePreferences(UserPreferenceEntity())
        }
    }
}