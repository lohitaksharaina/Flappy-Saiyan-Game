package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SaiyanAudioEngine
import com.example.data.SaiyanDatabase
import com.example.data.ScoreRecord
import com.example.data.ScoreRepository
import com.example.model.CharacterType
import com.example.model.SaiyanForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class FlappySaiyanViewModel(application: Application) : AndroidViewModel(application) {

    val audioEngine = SaiyanAudioEngine(application)
    private val repository: ScoreRepository

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var nextPipeId = 0L
    private var nextParticleId = 0L
    private var timeSinceLastPipe = 0f
    private val birdX = 80f
    private val birdRadius = 12f
    private val virtualWidth = 360f
    private val virtualHeight = 640f
    private val groundY = 560f

    val topScores: StateFlow<List<ScoreRecord>>

    init {
        val db = SaiyanDatabase.getDatabase(application)
        repository = ScoreRepository(db.scoreDao())
        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.highScore.collect { max ->
                _gameState.update { it.copy(highScore = max ?: 0) }
            }
        }

        // On game startup: trigger the 5-second Kamehameha audio track
        viewModelScope.launch {
            kotlinx.coroutines.delay(400)
            audioEngine.playStartKamehameha(CharacterType.GOKU)
        }
    }

    fun playStartKamehamehaTrack() {
        audioEngine.playStartKamehameha(_gameState.value.character)
        _gameState.update { it.copy(showStartMessage = true, startMessageTimer = 6.0f) }
    }

    fun switchCharacter(character: CharacterType) {
        if (_gameState.value.character == character) return
        audioEngine.stopCurrentAudio()
        _gameState.update {
            it.copy(
                character = character,
                currentForm = SaiyanForm.BASE,
                highestFormAchieved = SaiyanForm.BASE
            )
        }
    }

    fun setGameMode(mode: GameMode) {
        _gameState.update { it.copy(mode = mode) }
    }

    fun onFlapTap() {
        when (_gameState.value.status) {
            GameStatus.READY -> {
                audioEngine.playFlap()
                _gameState.update {
                    it.copy(
                        status = GameStatus.PLAYING,
                        birdVelocityY = -5.4f,
                        pipes = emptyList()
                    )
                }
                timeSinceLastPipe = 1.5f
            }
            GameStatus.PLAYING -> {
                audioEngine.playFlap()
                _gameState.update {
                    it.copy(
                        birdVelocityY = -5.4f
                    )
                }
            }
            GameStatus.GAME_OVER -> {
                // If game over, allow restart
                restartGame()
            }
        }
    }

    fun restartGame() {
        audioEngine.stopCurrentAudio()
        _gameState.update {
            it.copy(
                status = GameStatus.READY,
                currentForm = SaiyanForm.BASE,
                highestFormAchieved = SaiyanForm.BASE,
                score = 0,
                birdY = 300f,
                birdVelocityY = 0f,
                birdRotation = 0f,
                pipes = emptyList(),
                activeTransformationBanner = null,
                transformationBannerTimer = 0f
            )
        }
        timeSinceLastPipe = 0f
    }

    fun saveScoreToLeaderboard(playerName: String) {
        val state = _gameState.value
        val name = if (playerName.isBlank()) state.character.displayName else playerName.trim()
        viewModelScope.launch {
            repository.recordScore(
                ScoreRecord(
                    playerName = name,
                    score = state.score,
                    characterId = state.character.id,
                    characterName = state.character.displayName,
                    highestFormId = state.highestFormAchieved.formId,
                    highestFormName = state.highestFormAchieved.displayName,
                    gameMode = state.mode.title
                )
            )
        }
    }

    fun updateGameLoop(dt: Float) {
        val current = _gameState.value

        // Update start message timer
        val newStartTimer = if (current.startMessageTimer > 0f) current.startMessageTimer - dt else 0f
        val showStart = newStartTimer > 0f

        // Update transformation banner timer
        var banner = current.activeTransformationBanner
        var bannerTimer = current.transformationBannerTimer
        if (bannerTimer > 0f) {
            bannerTimer -= dt
            if (bannerTimer <= 0f) {
                banner = null
            }
        }

        // Particle and lightning updates
        val updatedParticles = updateParticles(current.particles, dt, current.currentForm, current.birdY)
        val updatedLightning = updateLightning(current.lightningBolts, current.currentForm, current.birdY)

        when (current.status) {
            GameStatus.READY -> {
                val bobbingY = 300f + sin(System.currentTimeMillis() / 250.0).toFloat() * 12f
                _gameState.update {
                    it.copy(
                        birdY = bobbingY,
                        birdVelocityY = 0f,
                        birdRotation = 0f,
                        particles = updatedParticles,
                        lightningBolts = updatedLightning,
                        showStartMessage = showStart,
                        startMessageTimer = newStartTimer,
                        activeTransformationBanner = banner,
                        transformationBannerTimer = bannerTimer,
                        groundOffset = (it.groundOffset + 1.5f) % 40f
                    )
                }
            }
            GameStatus.PLAYING -> {
                // Gravity & velocity - super gentle, floaty Saiyan flying
                val gravity = 12f * dt
                val newVelY = (current.birdVelocityY + gravity).coerceIn(-7.5f, 8f)
                val newBirdY = current.birdY + newVelY * (dt * 60f)

                // Rotation based on velocity
                val targetRot = when {
                    newVelY < -2f -> -15f
                    newVelY > 3f -> ((newVelY - 3f) * 6f).coerceAtMost(50f)
                    else -> 0f
                }
                val newRot = current.birdRotation + (targetRot - current.birdRotation) * 0.15f

                // Pipe management - speed decreases in Oozaru mode!
                // When in Super Saiyan 4 and Ultra Xeno form, size and speed revert to normal!
                timeSinceLastPipe += dt
                val currentPipes = current.pipes.map { it.copy() }.toMutableList()
                val baseSpeed = if (current.currentForm.isOozaru) 65f else 95f
                val pipeSpeed = baseSpeed * dt

                // Move pipes left
                for (pipe in currentPipes) {
                    pipe.x -= pipeSpeed
                }
                // Remove off-screen pipes
                currentPipes.removeAll { it.x < -it.width - 20f }

                // Spawn new pipe with huge gap (250f) and spacious interval
                val spawnInterval = if (current.currentForm.isOozaru) 2.6f else 2.2f
                if (timeSinceLastPipe >= spawnInterval) {
                    timeSinceLastPipe = 0f
                    val topH = Random.nextInt(90, 190).toFloat()
                    currentPipes.add(
                        PipeObstacle(
                            id = nextPipeId++,
                            x = virtualWidth + 30f,
                            topHeight = topH,
                            gap = 250f,
                            width = 50f
                        )
                    )
                }

                // Check scoring
                var newScore = current.score
                for (pipe in currentPipes) {
                    if (!pipe.scored && pipe.x + pipe.width < birdX) {
                        pipe.scored = true
                        newScore++
                        audioEngine.playScore()
                    }
                }

                // Check transformation trigger according to rules:
                // 5 -> SSJ1
                // 10 (next 5) -> SSJ2
                // 20 (next 10) -> SSJ3
                // 25 (next 5) -> SSG
                // 35 (next 10) -> SSB
                // 40 (next 5) -> UI (Goku) or UE (Vegeta)
                // 45 (next 5) -> MUI (Goku) or MUE (Vegeta)
                val targetForm = SaiyanForm.getFormForScore(newScore, current.character)
                var newForm = current.currentForm
                var highestForm = current.highestFormAchieved
                if (targetForm != current.currentForm) {
                    newForm = targetForm
                    highestForm = targetForm
                    banner = targetForm
                    bannerTimer = 4.0f
                    // Play 4-second transformation audio track!
                    audioEngine.playTransformationAudio(current.character, targetForm)
                }

                // High altitude flight: in PASS_THROUGH mode, the bird can fly as high as it wants!
                val clampedBirdY = if (current.mode == GameMode.PASS_THROUGH) {
                    newBirdY.coerceAtLeast(-120f)
                } else {
                    newBirdY
                }

                // Collision detection - exact circle-to-box intersection
                var collided = false
                // Ground collision: instant game over when bird touches ground in ALL modes
                if (clampedBirdY + birdRadius >= groundY) {
                    collided = true
                }
                // Ceiling collision: ONLY in CLASSIC mode!
                // In PASS_THROUGH mode, the bird can fly as high as it wants into the sky!
                if (current.mode == GameMode.CLASSIC && (clampedBirdY - birdRadius <= 0f)) {
                    collided = true
                }
                // Pipe collision: in CLASSIC mode, bird dies if it hits pole.
                // In PASS_THROUGH mode, bird phases through poles and dies ONLY on ground!
                if (current.mode == GameMode.CLASSIC) {
                    for (pipe in currentPipes) {
                        val topHit = circleIntersectsBox(
                            cx = birdX, cy = clampedBirdY, radius = birdRadius,
                            boxLeft = pipe.x, boxTop = 0f, boxRight = pipe.x + pipe.width, boxBottom = pipe.topHeight
                        )
                        val bottomHit = circleIntersectsBox(
                            cx = birdX, cy = clampedBirdY, radius = birdRadius,
                            boxLeft = pipe.x, boxTop = pipe.topHeight + pipe.gap, boxRight = pipe.x + pipe.width, boxBottom = groundY
                        )
                        if (topHit || bottomHit) {
                            collided = true
                            break
                        }
                    }
                }

                if (collided) {
                    // Trigger GAME OVER immediately
                    audioEngine.playHit()

                    // MANDATORY RULE: "when game is over transform the bird back into the base form and keep a record of leaderboard"
                    val finalState = current.copy(
                        status = GameStatus.GAME_OVER,
                        birdY = clampedBirdY.coerceAtMost(groundY - birdRadius),
                        birdVelocityY = 0f,
                        birdRotation = 70f,
                        currentForm = SaiyanForm.BASE, // Transformed back into base form!
                        highestFormAchieved = highestForm,
                        score = newScore,
                        activeTransformationBanner = null,
                        transformationBannerTimer = 0f
                    )
                    _gameState.value = finalState

                    // Auto-record to leaderboard
                    saveScoreToLeaderboard(current.character.displayName)
                } else {
                    _gameState.update {
                        it.copy(
                            birdY = clampedBirdY,
                            birdVelocityY = newVelY,
                            birdRotation = newRot,
                            pipes = currentPipes,
                            score = newScore,
                            currentForm = newForm,
                            highestFormAchieved = highestForm,
                            particles = updatedParticles,
                            lightningBolts = updatedLightning,
                            showStartMessage = showStart,
                            startMessageTimer = newStartTimer,
                            activeTransformationBanner = banner,
                            transformationBannerTimer = bannerTimer,
                            groundOffset = (it.groundOffset + pipeSpeed) % 40f
                        )
                    }
                }
            }
            GameStatus.GAME_OVER -> {
                _gameState.update {
                    it.copy(
                        particles = updatedParticles,
                        lightningBolts = emptyList(),
                        showStartMessage = showStart,
                        startMessageTimer = newStartTimer
                    )
                }
            }
        }
    }

    private fun updateParticles(
        existing: List<KiParticle>,
        dt: Float,
        form: SaiyanForm,
        birdY: Float
    ): List<KiParticle> {
        val result = existing.filter { it.life > 0f }.map {
            it.x += it.vx * dt
            it.y += it.vy * dt
            it.life -= dt * 1.5f
            it.alpha = (it.life * 0.9f).coerceIn(0f, 1f)
            it
        }.toMutableList()

        // Generate new particles around bird if transformed or base
        val spawnCount = if (form == SaiyanForm.BASE) 1 else 3
        if (result.size < 40 && Random.nextFloat() < 0.8f) {
            repeat(spawnCount) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val dist = Random.nextFloat() * 24f + 8f
                val px = birdX + kotlin.math.cos(angle) * dist
                val py = birdY + kotlin.math.sin(angle) * dist
                val color = if (Random.nextBoolean()) form.auraInnerColor.value.toLong() else form.auraOuterColor.value.toLong()
                result.add(
                    KiParticle(
                        id = nextParticleId++,
                        x = px,
                        y = py,
                        vx = (Random.nextFloat() - 0.5f) * 20f,
                        vy = -Random.nextFloat() * 45f - 15f,
                        size = Random.nextFloat() * 5f + 3f,
                        alpha = 0.9f,
                        color = color,
                        life = 1.0f
                    )
                )
            }
        }
        return result
    }

    private fun updateLightning(
        existing: List<LightningBolt>,
        form: SaiyanForm,
        birdY: Float
    ): List<LightningBolt> {
        if (!form.hasLightning) return emptyList()

        val active = existing.filter { it.alpha > 0.05f }.map {
            it.alpha -= 0.15f
            it
        }.toMutableList()

        if (active.size < 2 && Random.nextFloat() < 0.35f) {
            // Generate a zig-zag lightning arc around bird
            val startAngle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val startX = birdX + kotlin.math.cos(startAngle) * 28f
            val startY = birdY + kotlin.math.sin(startAngle) * 28f

            val pts = mutableListOf(androidx.compose.ui.geometry.Offset(startX, startY))
            var curX = startX
            var curY = startY
            repeat(4) {
                curX += (Random.nextFloat() - 0.5f) * 26f
                curY += (Random.nextFloat() - 0.5f) * 26f
                pts.add(androidx.compose.ui.geometry.Offset(curX, curY))
            }
            active.add(LightningBolt(id = Random.nextLong(), points = pts, alpha = 1.0f))
        }
        return active
    }

    private fun circleIntersectsBox(
        cx: Float, cy: Float, radius: Float,
        boxLeft: Float, boxTop: Float, boxRight: Float, boxBottom: Float
    ): Boolean {
        val nearestX = cx.coerceIn(boxLeft, boxRight)
        val nearestY = cy.coerceIn(boxTop, boxBottom)
        val deltaX = cx - nearestX
        val deltaY = cy - nearestY
        return (deltaX * deltaX + deltaY * deltaY) <= (radius * radius)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
