package com.hani.assistant.di

import android.content.Context
import com.hani.assistant.data.local.AppDatabase
import com.hani.assistant.data.local.ConversationDao
import com.hani.assistant.data.local.PreferenceDao
import com.hani.assistant.data.remote.ChatApi
import com.hani.assistant.repository.AppRepository
import com.hani.assistant.repository.ChatRepository
import com.hani.assistant.repository.ContactRepository
import com.hani.assistant.repository.NotificationRepository
import com.hani.assistant.services.ai.OpenAiChatService
import com.hani.assistant.services.speech.TextToSpeechEngine
import com.hani.assistant.services.wake.WakeWordEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideChatApi(client: OkHttpClient): ChatApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun providePreferenceDao(db: AppDatabase): PreferenceDao = db.preferenceDao()

    @Provides
    @Singleton
    fun provideWakeWordEngine(@ApplicationContext context: Context): WakeWordEngine {
        return WakeWordEngine(context)
    }

    @Provides
    @Singleton
    fun provideTextToSpeechEngine(@ApplicationContext context: Context): TextToSpeechEngine {
        return TextToSpeechEngine(context)
    }

    @Provides
    @Singleton
    fun provideOpenAiChatService(api: ChatApi): OpenAiChatService {
        return OpenAiChatService(api)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        appDatabase: AppDatabase,
        preferenceDao: PreferenceDao,
        conversationDao: ConversationDao
    ): AppRepository {
        return AppRepository(appDatabase, preferenceDao, conversationDao)
    }

    @Provides
    @Singleton
    fun provideContactRepository(@ApplicationContext context: Context): ContactRepository {
        return ContactRepository(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(@ApplicationContext context: Context): NotificationRepository {
        return NotificationRepository(context)
    }
}