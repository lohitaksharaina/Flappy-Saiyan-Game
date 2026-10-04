package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.model.CharacterType
import com.example.model.SaiyanForm
import kotlin.math.sin
import kotlin.random.Random

data class PipeObstacle(
    val id: Long,
    var x: Float,
    val topHeight: Float,
    val gap: Float = 250f,
    val width: Float = 50f,
    var scored: Boolean = false
) {
    fun getTopRect(): Rect = Rect(x, 0f, x + width, topHeight)
    fun getBottomRect(screenHeight: Float): Rect = Rect(x, topHeight + gap, x + width, screenHeight)
}

data class KiParticle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    val color: Long,
    var life: Float = 1.0f
)

data class LightningBolt(
    val id: Long,
    val points: List<Offset>,
    var alpha: Float = 1.0f
)

enum class GameMode(
    val id: String,
    val title: String,
    val subtitle: String
) {
    PASS_THROUGH(
        id = "pass_through",
        title = "PASS-THROUGH MODE",
        subtitle = "Phases through poles! Dies only on ground"
    ),
    CLASSIC(
        id = "classic",
        title = "CLASSIC MODE",
        subtitle = "Dies on pole hit or ground"
    )
}

enum class GameStatus {
    READY,
    PLAYING,
    GAME_OVER
}

data class GameState(
    val status: GameStatus = GameStatus.READY,
    val mode: GameMode = GameMode.PASS_THROUGH,
    val character: CharacterType = CharacterType.GOKU,
    val currentForm: SaiyanForm = SaiyanForm.BASE,
    val highestFormAchieved: SaiyanForm = SaiyanForm.BASE,
    val score: Int = 0,
    val highScore: Int = 0,
    val birdY: Float = 320f,
    val birdVelocityY: Float = 0f,
    val birdRotation: Float = 0f,
    val pipes: List<PipeObstacle> = emptyList(),
    val particles: List<KiParticle> = emptyList(),
    val lightningBolts: List<LightningBolt> = emptyList(),
    val activeTransformationBanner: SaiyanForm? = null,
    val transformationBannerTimer: Float = 0f,
    val showStartMessage: Boolean = true,
    val startMessageTimer: Float = 7.0f,
    val groundOffset: Float = 0f
)
