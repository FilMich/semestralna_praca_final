package com.example.semestralna_praca.ui.theme.screens

import com.example.semestralna_praca.viewmodel.HomeViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuestsScreen(viewModel: HomeViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    var showDialogForCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("🧾 Tvoje questy", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Kategórie:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        stats.forEach { (category, data) ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$category: Level ${data.level} (${data.xp}/100 XP)",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showDialogForCategory = category }) {
                        Icon(Icons.Default.Add, contentDescription = "Pridať quest")
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
        Text("Aktívne questy:", fontSize = 20.sp)

        if (activeQuests.isEmpty()) {
            Text("Zatiaľ nemáš žiadne aktívne questy.")
        } else {
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
                            viewModel.completeQuest(quest) { /* XP + level up */ }
                        }) {
                            Text("Označiť ako splnené")
                        }
                    }
                }
            }
        }
    }

    // Dialog na pridanie questu
    showDialogForCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { showDialogForCategory = null },
            title = { Text("Nový quest") },
            text = { Text("Chceš pridať quest pre kategóriu: $category?") },
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
                TextButton(onClick = { showDialogForCategory = null }) {
                    Text("Zrušiť")
                }
            }
        )
    }
}
