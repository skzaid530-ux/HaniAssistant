package com.hani.assistant.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactIndexer @Inject constructor(
    private val context: Context
) {
    private var contactList: List<ContactInfo> = emptyList()

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )
            val contacts = mutableListOf<ContactInfo>()
            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIndex)
                    val number = it.getString(numberIndex)
                    if (name != null && number != null) {
                        contacts.add(ContactInfo(name, number))
                    }
                }
            }
            contactList = contacts
        }
    }

    suspend fun findContact(query: String): ContactInfo? {
        if (contactList.isEmpty()) refresh()
        val lowerQuery = query.lowercase().trim()
        return contactList.find { it.name.lowercase().contains(lowerQuery) }
            ?: contactList.find { it.phoneNumber.replace(Regex("[\\s-]"), "").contains(lowerQuery) }
    }

    data class ContactInfo(val name: String, val phoneNumber: String)
}