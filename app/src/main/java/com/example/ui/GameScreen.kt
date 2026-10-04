package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.FlappySaiyanViewModel
import com.example.game.GameStatus
import com.example.model.CharacterType
import com.example.model.SaiyanForm

@Composable
fun GameScreen(
    viewModel: FlappySaiyanViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val topScores by viewModel.topScores.collectAsStateWithLifecycle()
    val activeVoiceLine by viewModel.audioEngine.currentVoiceLine.collectAsStateWithLifecycle()
    val isAudioPlaying by viewModel.audioEngine.isAudioPlaying.collectAsStateWithLifecycle()

    var showLeaderboard by remember { mutableStateOf(false) }
    var playerNameInput by remember { mutableStateOf("") }

    // Ultra-smooth Game Loop using withFrameNanos with jitter-smoothing filter
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        var smoothDt = 0.0166f
        while (true) {
            withFrameNanos { now ->
                val rawDt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                lastTime = now
                smoothDt = smoothDt * 0.75f + rawDt * 0.25f
                viewModel.updateGameLoop(smoothDt)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { viewModel.onFlapTap() }
            )
            .testTag("flap_touch_area")
    ) {
        // 1. Unified Game Canvas (Sky, Mountains, Poles, Ground, and Saiyan Character with authentic anime face)
        PipesAndEnvironment(
            character = state.character,
            form = state.currentForm,
            mode = state.mode,
            birdY = state.birdY,
            birdRotation = state.birdRotation,
            lightningBolts = state.lightningBolts,
            pipes = state.pipes,
            particles = state.particles,
            groundOffset = state.groundOffset
        )

        // 3. Heads-Up Display (HUD)
        GameHUD(
            state = state,
            isAudioPlaying = isAudioPlaying,
            voiceLine = activeVoiceLine,
            onOpenLeaderboard = { showLeaderboard = true },
            onSwitchCharacter = {
                val nextChar = if (state.character == CharacterType.GOKU) CharacterType.VEGETA else CharacterType.GOKU
                viewModel.switchCharacter(nextChar)
            },
            onSwitchMode = { viewModel.setGameMode(it) },
            onPlayKamehameha = { viewModel.playStartKamehamehaTrack() }
        )

        // 4. Opening Banner Message: "My First Anime Game -Lohitaksha Raina"
        AnimatedVisibility(
            visible = state.showStartMessage,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
        ) {
            StartAuthorMessageBanner(
                character = state.character,
                onReplayAudio = { viewModel.playStartKamehamehaTrack() }
            )
        }

        // 5. Epic Transformation Shockwave Banner
        state.activeTransformationBanner?.let { bannerForm ->
            TransformationBanner(
                form = bannerForm,
                character = state.character,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 6. Start Screen Overlay
        if (state.status == GameStatus.READY) {
            ReadyOverlay(
                character = state.character,
                currentMode = state.mode,
                onSwitchCharacter = {
                    val nextChar = if (state.character == CharacterType.GOKU) CharacterType.VEGETA else CharacterType.GOKU
                    viewModel.switchCharacter(nextChar)
                },
                onSelectMode = { viewModel.setGameMode(it) },
                onStart = { viewModel.onFlapTap() },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 7. Game Over Screen Overlay
        if (state.status == GameStatus.GAME_OVER) {
            GameOverOverlay(
                state = state,
                playerName = playerNameInput,
                onPlayerNameChange = { playerNameInput = it },
                onSaveScore = {
                    viewModel.saveScoreToLeaderboard(playerNameInput)
                },
                onRestart = { viewModel.restartGame() },
                onViewLeaderboard = { showLeaderboard = true },
                onSwitchCharacter = {
                    val nextChar = if (state.character == CharacterType.GOKU) CharacterType.VEGETA else CharacterType.GOKU
                    viewModel.switchCharacter(nextChar)
                },
                onSwitchMode = {
                    val nextMode = if (state.mode == com.example.game.GameMode.PASS_THROUGH) com.example.game.GameMode.CLASSIC else com.example.game.GameMode.PASS_THROUGH
                    viewModel.setGameMode(nextMode)
                },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // Leaderboard Modal Dialog
    if (showLeaderboard) {
        LeaderboardDialog(
            scores = topScores,
            onDismiss = { showLeaderboard = false }
        )
    }
}

@Composable
private fun GameHUD(
    state: com.example.game.GameState,
    isAudioPlaying: Boolean,
    voiceLine: String?,
    onOpenLeaderboard: () -> Unit,
    onSwitchCharacter: () -> Unit,
    onSwitchMode: (com.example.game.GameMode) -> Unit,
    onPlayKamehameha: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character & Form Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF140D26).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, state.currentForm.auraInnerColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(state.currentForm.primaryHairColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${state.character.displayName} • ${state.currentForm.displayName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${state.currentForm.japaneseName} • ${state.currentForm.powerMultiplier}",
                            color = state.currentForm.primaryHairColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Quick Actions: Mode toggle, Audio, Character Switch, Leaderboard
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Interactive Mode Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFFFFD54F).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F)
                    ),
                    modifier = Modifier
                        .clickable {
                            val next = if (state.mode == com.example.game.GameMode.PASS_THROUGH) com.example.game.GameMode.CLASSIC else com.example.game.GameMode.PASS_THROUGH
                            onSwitchMode(next)
                        }
                        .testTag("hud_game_mode_chip")
                ) {
                    Text(
                        text = if (state.mode == com.example.game.GameMode.PASS_THROUGH) "PHASING" else "MORTAL",
                        color = if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Replay Kamehameha Audio Button
                IconButton(
                    onClick = onPlayKamehameha,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF261845).copy(alpha = 0.85f))
                        .testTag("kamehameha_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Play Kamehameha Audio",
                        tint = if (isAudioPlaying) Color(0xFFFFEA00) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Switch Character Button
                IconButton(
                    onClick = onSwitchCharacter,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF261845).copy(alpha = 0.85f))
                        .testTag("switch_character_hud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChangeCircle,
                        contentDescription = "Switch Goku/Vegeta",
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Leaderboard Button
                IconButton(
                    onClick = onOpenLeaderboard,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF261845).copy(alpha = 0.85f))
                        .testTag("open_leaderboard_hud_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Open Leaderboard",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Score Counter
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${state.score}",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            // Next Transformation Threshold Gauge
            val nextThreshold = when {
                state.score < 5 -> 5
                state.score < 10 -> 10
                state.score < 20 -> 20
                state.score < 25 -> 25
                state.score < 35 -> 35
                state.score < 40 -> 40
                state.score < 45 -> 45
                else -> 45
            }

            val nextFormName = when (nextThreshold) {
                5 -> "Super Saiyan"
                10 -> "Super Saiyan 2"
                20 -> "Super Saiyan 3"
                25 -> "Super Saiyan God"
                35 -> "Super Saiyan Blue"
                40 -> if (state.character == CharacterType.GOKU) "Ultra Instinct" else "Ultra Ego"
                45 -> if (state.character == CharacterType.GOKU) "Mastered UI" else "Mastered UE"
                else -> "Max Divinity"
            }

            if (state.score < 45) {
                val prevThreshold = when (nextThreshold) {
                    5 -> 0
                    10 -> 5
                    20 -> 10
                    25 -> 20
                    35 -> 25
                    40 -> 35
                    45 -> 40
                    else -> 0
                }
                val progress = ((state.score - prevThreshold).toFloat() / (nextThreshold - prevThreshold).toFloat()).coerceIn(0f, 1f)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF140D26).copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Ki Surge",
                        tint = state.currentForm.primaryHairColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Next: $nextFormName (${state.score}/$nextThreshold poles)",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "MAXIMUM SAIYAN DIVINITY ACHIEVED",
                    color = Color(0xFFFFEA00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Live Anime Voice Subtitle Pill
        voiceLine?.let { line ->
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD000000))
                    .border(1.dp, state.currentForm.primaryHairColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Voice",
                        tint = state.currentForm.primaryHairColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\"$line\"",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Opening Banner Message explicitly requested:
 * "on the starting of the game display a small message 'My First Anime Game -Lohitaksha Raina'.and play audio tract of kamehameha for 5 seconds"
 */
@Composable
private fun StartAuthorMessageBanner(
    character: CharacterType,
    onReplayAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .testTag("start_author_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0E33)),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFFFFD700)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My First Anime Game -Lohitaksha Raina",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Playing 5s ${if (character == CharacterType.GOKU) "Kamehameha" else "Galick Gun"} blast audio!",
                    color = Color(0xFFCFD8DC),
                    fontSize = 11.sp
                )
            }
            IconButton(
                onClick = onReplayAudio,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6D00))
                    .testTag("replay_start_audio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Replay Kamehameha",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Dynamic Transformation Cutscene Banner
 */
@Composable
private fun TransformationBanner(
    form: SaiyanForm,
    character: CharacterType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_fx")
    val bannerScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_scale"
    )

    Card(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .scale(bannerScale)
            .testTag("transformation_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F081D).copy(alpha = 0.94f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            3.dp,
            Brush.horizontalGradient(
                listOf(form.auraInnerColor, form.primaryHairColor, form.auraOuterColor)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚡ TRANSFORMATION ⚡",
                color = Color(0xFFFFEA00),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = form.japaneseName,
                color = form.primaryHairColor,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )

            Text(
                text = form.displayName.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(form.primaryHairColor.copy(alpha = 0.25f))
                    .border(1.dp, form.primaryHairColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = form.powerMultiplier,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val quote = if (character == CharacterType.GOKU) form.voiceQuoteGoku else form.voiceQuoteVegeta
            if (quote.isNotBlank()) {
                Text(
                    text = "\"$quote\"",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ReadyOverlay(
    character: CharacterType,
    currentMode: com.example.game.GameMode,
    onSwitchCharacter: () -> Unit,
    onSelectMode: (com.example.game.GameMode) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .testTag("ready_overlay_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D26).copy(alpha = 0.92f)),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF6D00)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FLAPPY SAIYAN GAME",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Awaken The Divine Saiyan Powers",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Mode Selector (Pass-Through vs Classic)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1435),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (currentMode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GAMEPLAY MODE",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (currentMode == com.example.game.GameMode.PASS_THROUGH) "PHASING POLES" else "MORTAL POLES",
                            color = if (currentMode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSelectMode(com.example.game.GameMode.PASS_THROUGH) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_pass_through_mode_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentMode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00B0FF) else Color(0xFF261845)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "PASS-THRU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentMode == com.example.game.GameMode.PASS_THROUGH) Color.White else Color(0xFF90A4AE)
                            )
                        }

                        Button(
                            onClick = { onSelectMode(com.example.game.GameMode.CLASSIC) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_classic_mode_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentMode == com.example.game.GameMode.CLASSIC) Color(0xFFFF6D00) else Color(0xFF261845)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "CLASSIC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentMode == com.example.game.GameMode.CLASSIC) Color.White else Color(0xFF90A4AE)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentMode == com.example.game.GameMode.PASS_THROUGH)
                            "✨ Bird passes through poles! Dies ONLY on ground"
                        else
                            "💀 Hardcore: Bird dies on pole contact or ground",
                        color = Color(0xFFCFD8DC),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Character Selector Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF261845),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6D00)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSwitchCharacter() }
                    .testTag("ready_switch_character_pill")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SELECTED WARRIOR",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${character.displayName} (${if (character == CharacterType.GOKU) "Ultra Instinct Form" else "Ultra Ego Form"})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = onSwitchCharacter,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("switch_warrior_button")
                    ) {
                        Text("SWITCH", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transformation milestones preview
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F081D),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TRANSFORMATION PATHWAY",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 5 → Super Saiyan | 10 → SSJ2 | 20 → SSJ3\n" +
                                "• 25 → SSG | 35 → SSB\n" +
                                (if (character == CharacterType.GOKU)
                                    "• 40 → Ultra Instinct | 45 → Mastered UI\n"
                                else
                                    "• 40 → Ultra Ego | 45 → Mastered UE\n") +
                                "• 50 → Black Oozaru (Great Ape / slow speed)\n" +
                                "• 55 → Golden Oozaru (Golden Ape / slow speed)\n" +
                                "• 65 → Super Saiyan 4 (Normal size / normal speed)\n" +
                                "• 70 → White Oozaru (Divine Ape / slow speed)\n" +
                                "• 80 → Ultra Xeno ${character.displayName} (White Form / normal size)",
                        color = Color(0xFFECEFF1),
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_game_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TAP TO FLAP & FLY",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    state: com.example.game.GameState,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    onSaveScore: () -> Unit,
    onRestart: () -> Unit,
    onViewLeaderboard: () -> Unit,
    onSwitchCharacter: () -> Unit,
    onSwitchMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .testTag("game_over_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D26).copy(alpha = 0.95f)),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            Brush.verticalGradient(listOf(Color(0xFFFF1744), Color(0xFFFF6D00)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME OVER",
                color = Color(0xFFFF1744),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )

            Text(
                text = "Bird powered down to Base Form!",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Score and Highest Form Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF261845),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SCORE",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${state.score}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF261845),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BEST SCORE",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${maxOf(state.score, state.highScore)}",
                            color = Color(0xFFFFEA00),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Highest Form Reached Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F081D),
                border = androidx.compose.foundation.BorderStroke(1.dp, state.highestFormAchieved.primaryHairColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Peak Form Reached: ",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )
                    Text(
                        text = state.highestFormAchieved.displayName,
                        color = state.highestFormAchieved.primaryHairColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player Name input for Leaderboard
            OutlinedTextField(
                value = playerName,
                onValueChange = onPlayerNameChange,
                label = { Text("Player Name for Leaderboard") },
                placeholder = { Text(state.character.displayName) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFF455A64),
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color(0xFF90A4AE),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSaveScore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_score_button")
                ) {
                    Text("SAVE RECORD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onViewLeaderboard,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4527A0)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_leaderboard_button")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("HALL OF FAME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restart Button
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("restart_game_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RESTART BATTLE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Character Switcher on Game Over
            Button(
                onClick = onSwitchCharacter,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_over_switch_character_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261845)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ChangeCircle, contentDescription = "Switch", tint = Color(0xFFFFD54F))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SWITCH TO ${if (state.character == CharacterType.GOKU) "VEGETA" else "GOKU"}",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Mode Switcher on Game Over
            Button(
                onClick = onSwitchMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_over_switch_mode_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF0D2538) else Color(0xFF2A1C0A)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MODE: ${state.mode.title} (TAP TO SWITCH)",
                    color = if (state.mode == com.example.game.GameMode.PASS_THROUGH) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
