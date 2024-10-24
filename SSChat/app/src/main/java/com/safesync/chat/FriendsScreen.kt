package com.safesync.chat

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.safesync.chat.api.RetrofitClient
import com.safesync.chat.model.AddFriendRequest
import com.safesync.chat.model.FriendRequest
import com.safesync.chat.model.PendingRequest
import com.safesync.chat.model.RemoveFriendRequest
import com.safesync.chat.model.User
import com.safesync.chat.model.toUser
//import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FriendsScreen(navController: NavHostController) {
    var friends by remember { mutableStateOf(listOf<User>()) }
    var allUsernames by remember { mutableStateOf(listOf<String>()) }
    var pendingRequests by remember { mutableStateOf(emptyList<PendingRequest>()) }
    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val username = getLoggedInUsername(context) ?: "" // Fallback to empty string if no username is found

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                friends = fetchFriends(username)
                Log.d("FriendsScreen", "Fetched friends: $friends")

                allUsernames = fetchAllUsernames()
                Log.d("FriendsScreen", "Fetched all usernames: $allUsernames")

                pendingRequests = fetchPendingRequests(username)
                Log.d("FriendsScreen", "Fetched pending requests: $pendingRequests")
            } catch (e: Exception) {
                errorMessage = "Error fetching data: ${e.localizedMessage}"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Section for finding new friends
        Section(title = "Find New Friends") {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search for friends") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredUsernames = allUsernames.filter {
                it.contains(searchQuery, ignoreCase = true) && it != username
            }

            if (filteredUsernames.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredUsernames) { friendUsername ->
                        UserRow(
                            user = User(id = "", username = friendUsername, publicKey = null),
                            username = username,
                            onAddFriend = { success ->
                                if (success) {
                                    scope.launch {
                                        val requestSuccess = sendFriendRequest(username, friendUsername)
                                        if (!requestSuccess) {
                                            errorMessage = "Failed to send friend request."
                                        }
                                    }
                                } else {
                                    errorMessage = "Failed to send friend request."
                                }
                            }
                        )
                    }
                }
            } else {
                Text("No users found", modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section for pending friend requests
        Section(title = "Pending Friend Requests") {
            if (pendingRequests.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(pendingRequests) { request ->
                        PendingRequestRow(
                            request = request,
                            onAccept = { success ->
                                if (success) {
                                    // Remove the accepted request from the pending requests list
                                    pendingRequests = pendingRequests.filter { it.id != request.id }
                                    scope.launch {
                                        friends = fetchFriends(username) // Refresh friends list if needed
                                    }
                                } else {
                                    errorMessage = "Failed to accept friend request."
                                }
                            },
                            onDecline = { success ->
                                if (success) {
                                    // Remove the declined request from the pending requests list
                                    pendingRequests = pendingRequests.filter { it.id != request.id }
                                } else {
                                    errorMessage = "Failed to decline friend request."
                                }
                            }
                        )
                    }
                }
            } else {
                Text("No pending requests", modifier = Modifier.padding(16.dp))
            }
        }

        // Section for friends list
        Section(title = "Your Friends") {
            if (friends.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(friends) { friend ->
                        UserRow(
                            user = friend,
                            username = username,
                            onRemoveFriend = { success ->
                                if (success) {
                                    friends = friends.filter { it != friend }
                                } else {
                                    errorMessage = "Failed to remove friend."
                                }
                            },
                            onChatClick = { navController.navigate("chat/${username}/${friend.username}") },
                            isFriend = true
                        )
                    }
                }
            } else {
                Text("No friends found", modifier = Modifier.padding(16.dp))
            }
        }

        // Display error message if present
        errorMessage?.let {
            Text(text = it ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }

        // Logout button
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(onClick = {
                logout(context)
                navController.navigate("login")
            }) {
                Text("Logout")
            }
        }
    }
}
// Additional helper functions (UserRow, PendingRequestRow, Section, etc.) remain unchanged



@Composable
fun UserRow(
    user: User,
    username: String,
    onAddFriend: (Boolean) -> Unit = {},
    onRemoveFriend: (Boolean) -> Unit = {},
    onChatClick: () -> Unit = {},
    isFriend: Boolean = false
) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = user.username)
        Spacer(modifier = Modifier.weight(1f))

        if (!isFriend) {
            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true

                        scope.launch {
                            val success = sendFriendRequest(username, user.username)
                            isLoading = false
                            onAddFriend(success)
                        }
                    }
                },
                enabled = !isLoading // Prevent double clicks
            ) {
                Text(if (isLoading) "Sending..." else "Add Friend")
            }
        } else {
            Button(onClick = {
                scope.launch {
                    val success = removeFriend(username, user.username)
                    onRemoveFriend(success)
                }
            }) {
                Text("Remove Friend")
            }
            Button(onClick = onChatClick) {
                Text("Chat")
            }
        }
    }
}




@Composable
fun PendingRequestRow(
    request: PendingRequest,
    onAccept: (Boolean) -> Unit = {},
    onDecline: (Boolean) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val username = getLoggedInUsername(context) ?: "" // Provide a default value or handle null appropriately

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = request.senderUsername.ifEmpty { "Unknown" }) // Display sender's username
        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            scope.launch {
                val sender = request.senderUsername ?: ""
                val receiver = username // Use the logged-in username

                if (sender.isNotEmpty() && receiver.isNotEmpty()) {
                    val success = acceptFriendRequest(request, receiver) // Use the username as receiver
                    onAccept(success)
                } else {
                    Log.e("AcceptFriendRequest", "Sender or receiver username is empty")
                }
            }
        }) {
            Text("Accept")
        }


        Spacer(modifier = Modifier.width(8.dp))

        // Decline Button
        Button(onClick = {
            scope.launch {
                // Construct FriendRequest object for declining
                val friendRequest = FriendRequest(
                    id = request.id,
                    senderUsername = request.senderUsername,
                    receiverUsername = request.receiverUsername
                )

                // Call the API for declining
                val success = RetrofitClient.apiService.declineFriendRequest(friendRequest).isSuccessful
                onDecline(success)
            }
        }) {
            Text("Decline")
        }
    }
}





// Helper composable for sections
@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

// Helper functions to call your API using Retrofit



suspend fun sendFriendRequest(loggedInUsername: String, friendUsername: String): Boolean {
    val request = AddFriendRequest(
        senderUsername = loggedInUsername,
        receiverUsername = friendUsername // Make sure this is correct
    )

    val response = RetrofitClient.apiService.sendFriendRequest(request)

    if (!response.isSuccessful) {
        Log.e("SendFriendRequest", "Error: ${response.errorBody()?.string()}")
    }
    return response.isSuccessful
}

suspend fun fetchPendingRequests(username: String): List<PendingRequest> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getPendingRequests(username)
            Log.d("PendingRequestsResponse", response.body().toString())

            // Handle the response, mapping it to PendingRequest
            response.body()?.map { apiRequest ->
                PendingRequest(
                    id = apiRequest.id,
                    senderUsername = apiRequest.senderUsername ?: "", // Assign empty string if null
                    receiverUsername = apiRequest.receiverUsername ?: "" // Assign empty string if null
                )
            } ?: emptyList() // Return an empty list if the body is null
        } catch (e: Exception) {
            Log.e("FetchPendingRequests", "Error fetching pending requests: ${e.message}")
            emptyList()
        }
    }
}



suspend fun fetchFriends(username: String): List<User> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getFriends(username)
            response.body()?.map { apiUser ->
                apiUser.toUser() // Convert ApiUser to User using the extension function
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("FetchFriends", "Error fetching friends: ${e.message}")
            emptyList()
        }
    }
}

suspend fun fetchAllUsernames(): List<String> {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getAllUsernames() // Adjust this method in your Retrofit service interface
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FetchAllUsernames", "Error fetching usernames: ${e.message}")
            emptyList()
        }
    }
}


suspend fun removeFriend(username: String, friendUsername: String): Boolean {
    val request = RemoveFriendRequest(username, friendUsername)
    val response = RetrofitClient.apiService.removeFriend(request)

    if (!response.isSuccessful) {
        Log.e("RemoveFriend", "Error: ${response.errorBody()?.string()}")
    }
    return response.isSuccessful
}




suspend fun acceptFriendRequest(request: PendingRequest, receiverUsername: String): Boolean {
    Log.d("AcceptFriendRequest", "Sender: ${request.senderUsername}, Receiver: $receiverUsername")

    // Create a FriendRequest object
    val friendRequest = FriendRequest(
        id = request.id,
        senderUsername = request.senderUsername,
        receiverUsername = receiverUsername
    )

    // Call the API with the FriendRequest object
    val response = RetrofitClient.apiService.acceptFriendRequest(friendRequest)
    return response.isSuccessful
}



suspend fun addFriend(loggedInUsername: String, friendUsername: String): Boolean {
    val addFriendRequest = AddFriendRequest(
        senderUsername = loggedInUsername, // The logged-in user's username
        receiverUsername = friendUsername // The friend to be added
    )

    // Call your Retrofit API
    val response = RetrofitClient.apiService.addFriend(addFriendRequest) // Ensure this matches your API
    return response.isSuccessful
}

private fun logout(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        clear()
        apply()
    }
}

// Function to get the logged-in user's username
private fun getLoggedInUsername(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("username", null)
}
