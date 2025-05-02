package com.example.semestralna_praca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.semestralna_praca.model.LevelData
import com.example.semestralna_praca.model.Quest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests: StateFlow<List<Quest>> = _activeQuests

    private val _stats = MutableStateFlow<Map<String, LevelData>>(emptyMap())
    val stats: StateFlow<Map<String, LevelData>> = _stats

    private val _completedQuests = MutableStateFlow<List<Quest>>(emptyList())
    val completedQuests: StateFlow<List<Quest>> = _completedQuests

    init {
        loadUserStats()
        loadActiveQuests()
        loadCompletedQuests()
    }

    private fun loadUserStats() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val docRef = Firebase.firestore.collection("users").document(uid)

        viewModelScope.launch {
            docRef.get().addOnSuccessListener { document ->
                val statsMap = document.get("stats") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener
                val parsedStats = statsMap.mapValues { (_, value) ->
                    val level = (value["level"] as? Long)?.toInt() ?: 1
                    val xp = (value["xp"] as? Long)?.toInt() ?: 0
                    LevelData(level, xp)
                }
                _stats.value = parsedStats
            }
        }
    }

    fun addQuest(category: String, onComplete: () -> Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        val sampleQuests = mapOf(
            "strength" to listOf("Urob 20 drepov", "Zabehni 2 km"),
            "intelligence" to listOf("Prečítaj 10 strán knihy", "Nauč sa 5 nových slov"),
            "agility" to listOf("Skús jogu 15 min", "Skákaj cez švihadlo"),
            "creativity" to listOf("Nakresli obrázok", "Vymysli krátky príbeh"),
            "discipline" to listOf("Vstaň skôr o 30 min", "Naplánuj si deň")
        )

        val title = sampleQuests[category]?.random() ?: "Dokonči výzvu"
        val newQuest = mapOf(
            "category" to category,
            "title" to title,
            "done" to false,
            "xpReward" to 10
        )

        db.collection("users").document(uid)
            .collection("quests").add(newQuest)
            .addOnSuccessListener { onComplete() }
    }

    fun loadActiveQuests() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("users").document(uid)
            .collection("quests")
            .whereEqualTo("done", false)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val quests = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Quest(
                            id = doc.id,
                            category = data["category"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            done = data["done"] as? Boolean ?: false,
                            xpReward = (data["xpReward"] as? Long ?: 10L).toInt()
                        )
                    }
                    _activeQuests.value = quests
                }
            }
    }

    fun completeQuest(quest: Quest, onComplete: () -> Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore
        val docRef = db.collection("users").document(uid)
            .collection("quests").document(quest.id)

        docRef.update("done", true).addOnSuccessListener {
            val userRef = db.collection("users").document(uid)

            userRef.get().addOnSuccessListener { doc ->
                val statsMap = doc.get("stats") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener
                val current = statsMap[quest.category]
                val currentLevel = (current?.get("level") as? Long)?.toInt() ?: 1
                val currentXp = (current?.get("xp") as? Long)?.toInt() ?: 0

                val newXp = currentXp + quest.xpReward
                val newLevel = currentLevel + (newXp / 100)
                val remainingXp = newXp % 100

                val updated = statsMap.toMutableMap()
                updated[quest.category] = mapOf(
                    "level" to newLevel,
                    "xp" to remainingXp
                )

                userRef.update("stats", updated).addOnSuccessListener {
                    loadUserStats() // <-- Automatický refresh po update
                    onComplete()
                }
            }
        }
    }


    fun loadCompletedQuests() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("users").document(uid)
            .collection("quests")
            .whereEqualTo("done", true)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val quests = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Quest(
                            id = doc.id,
                            category = data["category"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            done = data["done"] as? Boolean ?: false,
                            xpReward = (data["xpReward"] as? Long ?: 10L).toInt()
                        )
                    }
                    _completedQuests.value = quests
                }
            }
    }

    fun deleteQuest(quest: Quest, onComplete: () -> Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        val userRef = db.collection("users").document(uid)

        // 1. Získaj aktuálne štatistiky
        userRef.get().addOnSuccessListener { doc ->
            val statsMap = doc.get("stats") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener
            val current = statsMap[quest.category]
            val currentLevel = (current?.get("level") as? Long)?.toInt() ?: 1
            val currentXp = (current?.get("xp") as? Long)?.toInt() ?: 0

            // 2. Odčítaj XP
            var newXp = currentXp - quest.xpReward
            var newLevel = currentLevel

            if (newXp < 0) {
                newLevel = (newLevel - 1).coerceAtLeast(1)
                newXp = 100 + newXp // napr. XP = -20 → zmení sa na 80 na predchádzajúcom leveli
            }

            val updated = statsMap.toMutableMap()
            updated[quest.category] = mapOf(
                "level" to newLevel,
                "xp" to newXp
            )

            // 3. Ulož štatistiky
            userRef.update("stats", updated).addOnSuccessListener {
                // 4. Zmaž quest
                db.collection("users").document(uid)
                    .collection("quests")
                    .document(quest.id)
                    .delete()
                    .addOnSuccessListener {
                        loadUserStats() // obnov štatistiky v UI
                        onComplete()
                    }
            }
        }
    }


}