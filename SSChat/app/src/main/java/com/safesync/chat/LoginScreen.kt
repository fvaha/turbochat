package com.safesync.chat

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.safesync.chat.api.RetrofitClient
import com.safesync.chat.model.LoginResponse
import com.safesync.chat.model.UserLogin

@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "LOGIN", fontSize = 32.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            loginUser(
                UserLogin(username, password),
                onSuccess = { loginResponse ->
                    saveLoggedInUser(navController.context, username) // Save username to shared preferences
                    navController.navigate("friends/${username}") // Navigate to FriendsScreen with username
                },
                onError = { message -> errorMessage = message }
            )
        }) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("registration")
        }) {
            Text(text = "Don't have an account? Register here", color = Color.Blue)
        }
    }
}

// Function to save the logged-in user
private fun saveLoggedInUser(context: Context, username: String) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("username", username) // Save the username
        apply() // Apply changes
    }
    Log.d("Login", "Logged in user: $username")
}

fun loginUser(
    userLogin: UserLogin,
    onSuccess: (LoginResponse) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.loginUser(userLogin)
            Log.d("LoginResponse", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.success) {
                    withContext(Dispatchers.Main) {
                        onSuccess(loginResponse)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("Login failed: ${loginResponse?.message ?: "Unknown error"}")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Login failed: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("An error occurred: ${e.message ?: "Unknown error"}")
            }
        }
    }
}
