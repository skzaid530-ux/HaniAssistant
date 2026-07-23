package com.hani.assistant.repository

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppIndexer @Inject constructor(
    private val context: Context
) {
    private var appList: List<AppInfo> = emptyList()

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, 0)
            appList = apps.map { resolveInfo ->
                AppInfo(
                    name = resolveInfo.loadLabel(pm).toString(),
                    packageName = resolveInfo.activityInfo.packageName
                )
            }
        }
    }

    suspend fun findApp(query: String): AppInfo? {
        if (appList.isEmpty()) refresh()
        val lowerQuery = query.lowercase().trim()
        return appList.find { it.name.lowercase().contains(lowerQuery) }
            ?: appList.find { it.packageName.lowercase().contains(lowerQuery) }
    }

    data class AppInfo(val name: String, val packageName: String)
}