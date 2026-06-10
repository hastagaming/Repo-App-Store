package com.repoappstore.data.api

import com.repoappstore.data.model.GitHubRelease
import com.repoappstore.data.model.GitHubRepository
import com.repoappstore.data.model.GitHubTokenResponse
import com.repoappstore.data.model.GitHubUser
import com.repoappstore.data.model.GitHubReadmeResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {
    // ========== Auth ==========
    @POST("https://github.com/login/oauth/access_token")
    @FormUrlEncoded
    suspend fun getOAuthToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): GitHubTokenResponse

    // ========== User ==========
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") auth: String
    ): GitHubUser

    // ========== Repositories ==========
    @GET("user/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") auth: String,
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "updated"
    ): List<GitHubRepository>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") auth: String? = null
    ): GitHubRepository

    // ========== Releases ==========
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Header("Authorization") auth: String? = null
    ): List<GitHubRelease>

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") auth: String? = null
    ): GitHubRelease

    // ========== README ==========
    @GET("repos/{owner}/{repo}/readme")
    suspend fun getReadme(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Accept") accept: String = "application/vnd.github.v3.raw",
        @Header("Authorization") auth: String? = null
    ): String

    // ========== Search ==========
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Header("Authorization") auth: String? = null
    ): SearchRepositoriesResponse
}

data class SearchRepositoriesResponse(
    val total_count: Int,
    val incomplete_results: Boolean,
    val items: List<GitHubRepository>
)