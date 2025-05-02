package com.example.semestralna_praca.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.semestralna_praca.ui.theme.screens.*

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Register : Screen("register")
    object Assessment : Screen("assessment")
    object Home : Screen("home")
    object Login : Screen("login")
}

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Assessment.route) {
            AssessmentScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
    }
}
