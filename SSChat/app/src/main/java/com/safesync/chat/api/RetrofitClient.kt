package com.safesync.chat.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.safesync.chat.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body user: UserRegistration): Response<Unit>

    @POST("login")
    suspend fun loginUser(@Body user: UserLogin): Response<LoginResponse>

    @GET("keys")
    suspend fun getPublicKey(@Query("username") username: String): Response<PublicKeyResponse>

    @POST("send_message")
    suspend fun sendMessage(@Body message: Message): Response<Unit>

    @GET("messages")
    suspend fun getMessages(
        @Query("sender") sender: String,
        @Query("recipient") recipient: String
    ): Response<List<Message>>

    @POST("add_friend")
    suspend fun addFriend(@Body request: AddFriendRequest): Response<Unit> // Ensure it returns the correct response type


    @POST("remove_friend")
    suspend fun removeFriend(@Body request: RemoveFriendRequest): Response<Unit>

    @GET("friends")
    suspend fun getFriends(@Query("username") username: String): Response<List<ApiUser>>

    @GET("users") // This should match your server endpoint
    suspend fun getAllUsernames(): Response<List<String>> // Expecting a list of strings (usernames)

    @GET("pending_friend_requests")
    suspend fun getPendingRequests(@Query("username") username: String): Response<List<PendingRequest>>

    @POST("send_friend_request")
    suspend fun sendFriendRequest(@Body request: AddFriendRequest): Response<Unit>

    @POST("accept_friend_request")
    suspend fun acceptFriendRequest(@Body request: FriendRequest): Response<Unit>


    @POST("decline_friend_request")
    suspend fun declineFriendRequest(@Body request: FriendRequest): Response<Unit> // Match the response type



}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080" // Replace with your server URL

    private val gson: Gson = GsonBuilder().create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
