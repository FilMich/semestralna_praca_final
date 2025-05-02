package com.example.semestralna_praca.ui.theme.screens

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.semestralna_praca.model.Quest
import com.example.semestralna_praca.navigation.Screen
import com.example.semestralna_praca.viewmodel.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val completedQuests by viewModel.completedQuests.collectAsState()
    var questToDelete by remember { mutableStateOf<Quest?>(null) }

    var showDialogForCategory by remember { mutableStateOf<String?>(null) }

    showDialogForCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { showDialogForCategory = null },
            title = { Text("New Quest") },
            text = { Text("Pridať quest pre kategóriu: $category?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addQuest(category) {
                        showDialogForCategory = null
                    }
                }) {
                    Text("Pridať")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialogForCategory = null
                }) {
                    Text("Zrušiť")
                }
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start


    ) {
        Text("Welcome, Hero!", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Stats:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        stats.forEach { (category, data) ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$category: Level ${data.level} (${data.xp}/100 XP)", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDialogForCategory = category }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Quest")
                    }
                }

                LinearProgressIndicator(
                    progress = data.xp / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }


        Spacer(modifier = Modifier.height(24.dp))
        Text("Active Quests:", fontSize = 20.sp)

        activeQuests.forEach { quest ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${quest.title} [${quest.category}]")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.completeQuest(quest) { /* reload state if needed */ }
                    }) {
                        Text("Mark as Done")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Completed Quests:", fontSize = 20.sp)

        if (completedQuests.isEmpty()) {
            Text("No completed quests yet.")
        } else {
            completedQuests.forEach { quest ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "✔ ${quest.title} [${quest.category}]",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        questToDelete = quest
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Quest")
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            Firebase.auth.signOut()
            navController.navigate(Screen.Welcome.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }) {
            Text("Log out")
        }
        questToDelete?.let { quest ->
            AlertDialog(
                onDismissRequest = { questToDelete = null },
                title = { Text("Zmazať quest") },
                text = {
                    Text("Chceš zmazať tento quest?\n\n${quest.title} [${quest.category}]\n\nStratíš ${quest.xpReward} XP.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteQuest(quest) {
                            questToDelete = null
                        }
                    }) {
                        Text("Zmazať")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { questToDelete = null }) {
                        Text("Zrušiť")
                    }
                }
            )
        }
    }
}