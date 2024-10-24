package com.safesync.chat.model

import com.google.gson.annotations.SerializedName

data class UserLogin(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)

data class UserData(
    val id: Long,
    val username: String,
    val publicKey: String // Change to publicKey for consistency with User
)

data class UserRegistration(
    val username: String,
    val password: String,
    val publickey1: String,
    val publickey2: String,
    val publickey3: String,
    val publickey4: String
)

data class User(
    val id: String,            // Ensure this matches the API response type
    val username: String,
    val publicKey: String?,    // Make nullable in case the user doesn't have a public key
    val friends: List<User>? = null // Nullable list of friends for many-to-many relationship
)

data class ApiUser(
    val id: String,            // Ensure this matches your API response
    val username: String,
    @SerializedName("publickey") val publicKey: String, // Use 'publickey' to match the API response field
    val friends: List<ApiUser>? = null // Use ApiUser to match the structure from the API
)

// Extension function to convert ApiUser to User
fun ApiUser.toUser(): User {
    return User(
        id = this.id,             // Pass id directly
        username = this.username, // Pass username directly
        publicKey = this.publicKey, // Match the field name with User class
        friends = null            // Set friends to null or handle as needed
    )
}

data class Message(
    val sender: String,
    val recipient: String,
    val content: String
)

data class PublicKeyResponse(
    val publicKey: String // Adjusted to match the naming convention
)

data class AddFriendRequest(
    @SerializedName("sender_username") val senderUsername: String,
    @SerializedName("receiver_username") val receiverUsername: String // Ensure you have both sender and receiver usernames
)


data class FriendRequest(
    val id: Int,
    @SerializedName("sender_username") val senderUsername: String,
    @SerializedName("receiver_username") val receiverUsername: String
)


data class PendingRequest(
    val id: Int,
    val senderUsername: String,
    val receiverUsername: String
)


data class DeclineFriendRequestResponse(
    val status: String
)


data class RemoveFriendRequest(
    val username: String,
    val friendUsername: String
)

data class Friend(
    val username: String // This only contains username
)

data class FriendListResponse(
    val friends: List<Friend>
)
