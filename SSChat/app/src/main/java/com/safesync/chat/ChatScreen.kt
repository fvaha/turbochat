package com.safesync.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.safesync.chat.api.RetrofitClient
import com.safesync.chat.model.Message
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(navController: NavController, username: String, recipient: String) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val coroutineScope = rememberCoroutineScope()

    // Function to fetch messages between users
    fun fetchMessages() {
        coroutineScope.launch {
            val response = RetrofitClient.apiService.getMessages(sender = username, recipient = recipient)
            if (response.isSuccessful) {
                response.body()?.let { fetchedMessages ->
                    messages.clear()
                    messages.addAll(fetchedMessages)
                }
            } else {
                // Handle the error
                println("Failed to fetch messages: ${response.errorBody()?.string()}")
            }
        }
    }

    // Function to send a message
    fun sendMessage() {
        if (messageText.isBlank()) return // Prevent sending empty messages
        coroutineScope.launch {
            val message = Message(sender = username, recipient = recipient, content = messageText)
            val response = RetrofitClient.apiService.sendMessage(message)
            if (response.isSuccessful) {
                // Clear the input field after sending
                messageText = ""
                // Re-fetch messages after sending to update the UI
                fetchMessages()
            } else {
                // Handle the error
                println("Failed to send message: ${response.errorBody()?.string()}")
            }
        }
    }

    // Fetch messages when the screen is first displayed
    LaunchedEffect(Unit) {
        fetchMessages()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chat with $recipient", style = MaterialTheme.typography.headlineMedium)

        Box(modifier = Modifier.weight(1f)) {  // Container for messages to allow scrolling
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false // Newest message at the bottom, older messages at the top
            ) {
                items(messages.size) { index ->
                    val message = messages[index]
                    val isCurrentUser = message.sender == username

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isCurrentUser) Arrangement.Start else Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    color = if (isCurrentUser) Color(0xFFDCF8C6) else Color(0xFF34B7F1), // Green for current user, blue for others
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(12.dp)
                                .widthIn(max = 240.dp)
                        ) {
                            Text(
                                text = message.content,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp)
            )

            Button(onClick = { sendMessage() }) {
                Text("Send")
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Add some space above the back button

        // Back button to navigate to FriendsScreen
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
