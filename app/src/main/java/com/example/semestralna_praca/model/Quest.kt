package com.example.semestralna_praca.model

data class Quest(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val done: Boolean = false,
    val xpReward: Int = 10
)