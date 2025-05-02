package com.example.semestralna_praca.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.semestralna_praca.ui.theme.screens.HistoryScreen
import com.example.semestralna_praca.ui.theme.screens.ProfileScreen
import com.example.semestralna_praca.ui.theme.screens.QuestsScreen
import com.example.semestralna_praca.viewmodel.HomeViewModel

@Composable
fun MainScreen(rootNavController: NavHostController, viewModel: HomeViewModel) {
    val navController = rememberNavController()
    var selectedRoute by remember { mutableStateOf("profile") }
    val items = listOf(
        BottomNavItem.Profile,
        BottomNavItem.Quests,
        BottomNavItem.History
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                                viewModel.loadUserStats()
                                viewModel.loadActiveQuests()
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Profile.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = rootNavController)
            }
            composable(BottomNavItem.Quests.route) {
                QuestsScreen()
            }
            composable(BottomNavItem.History.route) {
                HistoryScreen()
            }
        }
    }
}
