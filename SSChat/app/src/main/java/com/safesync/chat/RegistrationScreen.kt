package com.safesync.chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.safesync.chat.model.UserRegistration
import kotlin.random.Random

@Composable
fun RegistrationScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register", fontSize = 32.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (password == confirmPassword) {
                val randomKeys = generateRandomKeys() // Generate four random keys

                registerUser(
                    username,
                    password,
                    randomKeys[0],
                    randomKeys[1],
                    randomKeys[2],
                    randomKeys[3],
                    navController,
                    onSuccess = {
                        successMessage = "Registration successful"
                    },
                    onError = { message ->
                        errorMessage = message
                    }
                )
            } else {
                errorMessage = "Passwords do not match"
            }
        }) {
            Text(text = "Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { Text(text = it, color = Color.Red) }
        successMessage?.let { Text(text = it, color = Color.Green) }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("login")
        }) {
            Text(text = "Already have an account? Login here", color = Color.Blue)
        }
    }
}

// Function to generate four random public keys
fun generateRandomKeys(): List<String> {
    val keys = mutableListOf<String>()

    repeat(4) {
        // Generate a 5-digit key
        val key = Random.nextInt(10000, 100000).toString() // Generates a random integer in the range 10000 to 99999
        keys.add(key)
    }

    // Log the generated keys for debugging purposes
    Log.d("RandomKeys", "Generated keys: $keys")

    return keys // Ensure the list of keys is returned
}

// ovde ide RUST SDK generate keys poziv
/*
 fun callRustFunction() {
        val keys = generateUserKeys() // Call the Rust function
        // Process the returned keys as needed
    }


    val rustIntegration = RustIntegration()
        val keys = rustIntegration.generateUserKeys() // Call the Rust function

        // Split keys if necessary, based on your expected format
        val keyList = keys.split(",")
        if (keyList.size == 4) {
}
*/

fun registerUser(
    username: String,
    password: String,
    publickey1: String,
    publickey2: String,
    publickey3: String,
    publickey4: String,
    navController: NavHostController,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        val user = UserRegistration(
            username = username,
            password = password,
            publickey1 = publickey1,
            publickey2 = publickey2,
            publickey3 = publickey3,
            publickey4 = publickey4
        )

        try {
            val response = RetrofitClient.apiService.registerUser(user)
            Log.d("RegistrationResponse", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                    navController.navigate("friends/$username")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RegistrationError", "Error body: $errorBody")
                withContext(Dispatchers.Main) {
                    onError("Registration failed: ${errorBody ?: "Unknown error"}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("An error occurred: ${e.message}")
            }
        }
    }
}
