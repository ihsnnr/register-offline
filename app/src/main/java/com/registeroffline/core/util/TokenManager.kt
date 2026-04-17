package com.registeroffline.core.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun getToken(): String? = context.dataStore.data.first()[KEY_TOKEN]

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun saveProfile(fullName: String, email: String) {
        context.dataStore.edit {
            it[KEY_FULL_NAME] = fullName
            it[KEY_EMAIL] = email
        }
    }

    val fullNameFlow: Flow<String> = context.dataStore.data.map { it[KEY_FULL_NAME] ?: "" }
    val emailFlow: Flow<String> = context.dataStore.data.map { it[KEY_EMAIL] ?: "" }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
