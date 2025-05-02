package com.example.semestralna_praca.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.AccountCircle)
    object Quests : BottomNavItem("quests", "Quests", Icons.Filled.List)
    object History : BottomNavItem("history", "History", Icons.Filled.CheckCircle)
}