package com.repoappstore.data.auth

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlin.random.Random

private val Context.dataStore by preferencesDataStore("auth")

class GitHubAuthService(private val context: Context) {
    private val tokenKey = stringPreferencesKey("access_token")
    private val userKey = stringPreferencesKey("user_login")
    private val tokenTypeKey = stringPreferencesKey("token_type")
    private val scopeKey = stringPreferencesKey("scope")

    suspend fun saveToken(
        token: String,
        tokenType: String,
        scope: String,
        userLogin: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
            preferences[tokenTypeKey] = tokenType
            preferences[scopeKey] = scope
            preferences[userKey] = userLogin
        }
    }

    suspend fun getToken(): String? {
        val preferences = context.dataStore.data.value
        return preferences[tokenKey]
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences.remove(tokenTypeKey)
            preferences.remove(scopeKey)
            preferences.remove(userKey)
        }
    }

    suspend fun getAuthHeader(): String? {
        val token = getToken() ?: return null
        return "Bearer $token"
    }

    fun generateOAuthState(): String = UUID.randomUUID().toString()

    fun getOAuthUrl(
        clientId: String,
        redirectUri: String,
        state: String,
        scopes: String = "public_repo,user"
    ): String {
        return "https://github.com/login/oauth/authorize?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "scope=$scopes&" +
                "state=$state"
    }

    fun parseCallbackUri(uri: Uri): Pair<String?, String?> {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        return Pair(code, state)
    }
}