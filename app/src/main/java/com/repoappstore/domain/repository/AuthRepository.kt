package com.repoappstore.domain.repository

import com.repoappstore.BuildConfig
import com.repoappstore.data.api.GitHubApiService
import com.repoappstore.data.auth.GitHubAuthService
import com.repoappstore.data.model.AuthUser
import com.repoappstore.data.model.GitHubUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: GitHubApiService,
    private val authService: GitHubAuthService
) {

    // ========== OAuth Flow ==========
    fun getOAuthUrl(): Pair<String, String> {
        val state = authService.generateOAuthState()
        val url = authService.getOAuthUrl(
            clientId = BuildConfig.GITHUB_CLIENT_ID,
            redirectUri = BuildConfig.GITHUB_REDIRECT_URI,
            state = state
        )
        return Pair(url, state)
    }

    fun exchangeOAuthCode(
        code: String,
        expectedState: String,
        receivedState: String
    ): Flow<Result<AuthUser>> = flow {
        if (expectedState != receivedState) {
            emit(Result.failure(Exception("OAuth state mismatch, possible CSRF attack")))
            return@flow
        }
        try {
            val tokenResponse = apiService.getOAuthToken(
                clientId = BuildConfig.GITHUB_CLIENT_ID,
                clientSecret = BuildConfig.GITHUB_CLIENT_SECRET,
                code = code,
                redirectUri = BuildConfig.GITHUB_REDIRECT_URI
            )
            val authHeader = "Bearer ${tokenResponse.access_token}"
            val user = apiService.getCurrentUser(authHeader)
            authService.saveToken(
                token = tokenResponse.access_token,
                tokenType = tokenResponse.token_type,
                scope = tokenResponse.scope,
                userLogin = user.login
            )
            emit(Result.success(user.toAuthUser(tokenResponse.access_token, tokenResponse.token_type)))
        } catch (e: Exception) {
            Timber.e(e, "OAuth exchange gagal")
            emit(Result.failure(e))
        }
    }

    // ========== PAT Flow ==========
    fun loginWithPat(token: String): Flow<Result<AuthUser>> = flow {
        try {
            val authHeader = "Bearer $token"
            val user = apiService.getCurrentUser(authHeader)
            authService.saveToken(
                token = token,
                tokenType = "bearer",
                scope = "pat",
                userLogin = user.login
            )
            emit(Result.success(user.toAuthUser(token, "bearer")))
        } catch (e: Exception) {
            Timber.e(e, "PAT login failed")
            emit(Result.failure(Exception("Token is invalid or does not have access")))
        }
    }

    // ========== Session ==========
    fun getCurrentSession(): Flow<Result<AuthUser?>> = flow {
        try {
            val token = authService.getToken()
            if (token == null) {
                emit(Result.success(null))
                return@flow
            }
            val authHeader = "Bearer $token"
            val user = apiService.getCurrentUser(authHeader)
            emit(Result.success(user.toAuthUser(token, "bearer")))
        } catch (e: Exception) {
            Timber.e(e, "Failed to load session")
            emit(Result.success(null))
        }
    }

    suspend fun logout() {
        authService.clearToken()
    }

    suspend fun getAuthHeader(): String? {
        return authService.getAuthHeader()
    }

    private fun GitHubUser.toAuthUser(token: String, tokenType: String): AuthUser {
        return AuthUser(
            login = login,
            id = id,
            avatarUrl = avatar_url,
            name = name,
            bio = bio,
            publicRepos = public_repos,
            followers = followers,
            token = token,
            tokenType = tokenType
        )
    }
}