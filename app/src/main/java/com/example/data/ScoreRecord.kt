package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_scores")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playerName: String,
    val score: Int,
    val characterId: String,
    val characterName: String,
    val highestFormId: String,
    val highestFormName: String,
    val gameMode: String = "pass_through",
    val timestamp: Long = System.currentTimeMillis()
)
