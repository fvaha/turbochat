package com.safesync.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safesync.chat.ui.theme.SSChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SSChatTheme {
                AppNavGraph()
            }
        }
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("registration") { RegistrationScreen(navController) }
        composable("friends/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: return@composable
            FriendsScreen(navController)
        }
        composable("chat/{username}/{recipient}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: return@composable
            val recipient = backStackEntry.arguments?.getString("recipient") ?: return@composable
            ChatScreen(navController, username, recipient)
        }
    }
}