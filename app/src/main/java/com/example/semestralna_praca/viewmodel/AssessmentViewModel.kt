package com.example.semestralna_praca.viewmodel

import androidx.lifecycle.ViewModel
import com.example.semestralna_praca.model.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AssessmentViewModel : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _answers = mutableMapOf<Int, Int>()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        _questions.value = listOf(
            Question(1, "strength", "Ako často cvičíš?", listOf("Nikdy", "Občas", "Pravidelne"), listOf(1, 3, 5)),
            Question(2, "intelligence", "Ako často čítaš knihy?", listOf("Nikdy", "Občas", "Denne"), listOf(1, 3, 5)),
            Question(3, "creativity", "Venuješ sa tvorbe (hudba, kresba...)?", listOf("Vôbec", "Zriedkavo", "Často"), listOf(1, 2, 5)),
            // pridaj viac podľa potreby...
        )
    }

    fun answerQuestion(id: Int, score: Int) {
        _answers[id] = score
    }

    fun calculateCategoryScores(): Map<String, Int> {
        val scoresByCategory = mutableMapOf<String, MutableList<Int>>()

        for (q in _questions.value) {
            val score = _answers[q.id] ?: 0
            scoresByCategory.getOrPut(q.category) { mutableListOf() }.add(score)
        }

        return scoresByCategory.mapValues { (_, scores) ->
            scores.average().roundToInt().coerceIn(1, 5)
        }
    }

    fun saveResultsToFirestore(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val currentUser = Firebase.auth.currentUser
        val scores = calculateCategoryScores()

        if (currentUser != null) {
            val db = Firebase.firestore
            val userStatsRef = db.collection("users").document(currentUser.uid)

            val scores = calculateCategoryScores() // Map<String, Int>

            val structuredStats = scores.mapValues { (_, level) ->
                mapOf("level" to level, "xp" to 0)
            }

            val data = mapOf("stats" to structuredStats)

            userStatsRef.set(data)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e) }
        } else {
            onError(Exception("No user logged in"))
        }
    }
}
