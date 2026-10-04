package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM leaderboard_scores ORDER BY score DESC, timestamp DESC LIMIT 50")
    fun getTopScores(): Flow<List<ScoreRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreRecord): Long

    @Query("SELECT MAX(score) FROM leaderboard_scores")
    fun getHighScore(): Flow<Int?>

    @Query("DELETE FROM leaderboard_scores")
    suspend fun clearLeaderboard()
}
