package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    val topScores: Flow<List<ScoreRecord>> = scoreDao.getTopScores()
    val highScore: Flow<Int?> = scoreDao.getHighScore()

    suspend fun recordScore(score: ScoreRecord): Long {
        return scoreDao.insertScore(score)
    }

    suspend fun resetLeaderboard() {
        scoreDao.clearLeaderboard()
    }
}
