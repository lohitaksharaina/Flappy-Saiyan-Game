package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.game.GameMode
import com.example.game.KiParticle
import com.example.game.LightningBolt
import com.example.game.PipeObstacle
import com.example.model.CharacterType
import com.example.model.SaiyanForm

@Composable
fun PipesAndEnvironment(
    character: CharacterType,
    form: SaiyanForm,
    mode: GameMode = GameMode.PASS_THROUGH,
    birdY: Float,
    birdRotation: Float,
    lightningBolts: List<LightningBolt>,
    pipes: List<PipeObstacle>,
    particles: List<KiParticle>,
    groundOffset: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "game_canvas_anim")
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "canvas_aura_pulse"
    )
    val auraRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "canvas_aura_rotate"
    )
    val wingFlap by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "canvas_wing_flap"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Virtual coords scale: virtual world is 360 x 640
        val scaleX = w / 360f
        val scaleY = h / 640f

        // 1. Dynamic Anime Sky Gradient
        drawAnimeSky(w, h, form)

        // 2. Distant Wasteland / Tournament Mountains
        drawDistantMountains(w, h * 0.72f, form)

        // 3. Floating Ki Particles in Environment
        drawEnvironmentParticles(particles, scaleX, scaleY)

        // 4. Energy Poles / Pillars
        drawEnergyPoles(pipes, scaleX, scaleY, h, form)

        // 5. Martial Arts Arena Ground
        val groundTop = 560f * scaleY
        drawArenaGround(w, h, groundTop, groundOffset * scaleX, form)

        // 6. Draw Saiyan Character Bird
        // In Oozaru mode, size of the oozaru monkey becomes a little bigger!
        // When it becomes super saiyan 4 and ultra xeno form, size reverts to normal!
        val oozaruScale = if (form.isOozaru) 1.45f else 1.0f
        val birdRadius = 22f * scaleX * oozaruScale
        val birdCenterX = 80f * scaleX
        val birdCenterY = birdY * scaleY

        drawSaiyanBirdCharacter(
            centerX = birdCenterX,
            centerY = birdCenterY,
            radius = birdRadius,
            character = character,
            form = form,
            rotation = birdRotation,
            auraPulse = auraPulse,
            auraRotate = auraRotate,
            wingFlap = wingFlap,
            lightningBolts = lightningBolts
        )

        // 7. High Altitude Ki Beacon if bird is soaring high in upper atmosphere
        if (birdCenterY < 30f) {
            val indicatorY = 20f
            drawCircle(
                color = form.auraInnerColor.copy(alpha = 0.85f),
                radius = 14f * auraPulse,
                center = Offset(birdCenterX, indicatorY)
            )
            drawCircle(
                color = form.primaryHairColor,
                radius = 8f,
                center = Offset(birdCenterX, indicatorY)
            )
            val arrowPath = Path().apply {
                moveTo(birdCenterX, indicatorY - 10f)
                lineTo(birdCenterX - 7f, indicatorY + 3f)
                lineTo(birdCenterX + 7f, indicatorY + 3f)
                close()
            }
            drawPath(arrowPath, color = Color.White)
        }
    }
}

private fun DrawScope.drawAnimeSky(w: Float, h: Float, form: SaiyanForm) {
    val skyColors = when (form) {
        SaiyanForm.BASE -> listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF81D4FA))
        SaiyanForm.SUPER_SAIYAN -> listOf(Color(0xFF3E2723), Color(0xFFE65100), Color(0xFFFFD54F))
        SaiyanForm.SUPER_SAIYAN_2 -> listOf(Color(0xFF263238), Color(0xFFFF8F00), Color(0xFFFFF176))
        SaiyanForm.SUPER_SAIYAN_3 -> listOf(Color(0xFF212121), Color(0xFFBF360C), Color(0xFFFFB300))
        SaiyanForm.SUPER_SAIYAN_GOD -> listOf(Color(0xFF311B92), Color(0xFF880E4F), Color(0xFFFF4081))
        SaiyanForm.SUPER_SAIYAN_BLUE -> listOf(Color(0xFF001026), Color(0xFF004D40), Color(0xFF00E5FF))
        SaiyanForm.ULTRA_INSTINCT, SaiyanForm.MASTERED_ULTRA_INSTINCT -> listOf(
            Color(0xFF050510),
            Color(0xFF1A237E),
            Color(0xFFCFD8DC)
        )
        SaiyanForm.ULTRA_EGO, SaiyanForm.MASTERED_ULTRA_EGO -> listOf(
            Color(0xFF120024),
            Color(0xFF4A148C),
            Color(0xFFBA68C8)
        )
        SaiyanForm.BLACK_OOZARU -> listOf(
            Color(0xFF0A0000),
            Color(0xFF260505),
            Color(0xFFB71C1C) // Blood Moon Sky
        )
        SaiyanForm.GOLDEN_OOZARU -> listOf(
            Color(0xFF1B0500),
            Color(0xFF5D4037),
            Color(0xFFFFD54F) // Golden Primal Tempest
        )
        SaiyanForm.SUPER_SAIYAN_4 -> listOf(
            Color(0xFF1B0000),
            Color(0xFF880E4F),
            Color(0xFFFF6D00) // Crimson Twilight
        )
        SaiyanForm.WHITE_OOZARU -> listOf(
            Color(0xFF0F081D),
            Color(0xFF4A148C),
            Color(0xFFEDE7F6) // Astral Silver Night
        )
        SaiyanForm.ULTRA_XENO -> listOf(
            Color(0xFF000814),
            Color(0xFF002171),
            Color(0xFFE1F5FE) // Space-Time Divine Sky
        )
    }

    drawRect(
        brush = Brush.verticalGradient(skyColors, startY = 0f, endY = h),
        size = Size(w, h)
    )

    // Sun / Celestial Ki source in the sky
    val sunCenter = Offset(w * 0.78f, h * 0.16f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(form.auraInnerColor.copy(alpha = 0.6f), Color.Transparent),
            center = sunCenter,
            radius = w * 0.4f
        ),
        radius = w * 0.4f,
        center = sunCenter
    )
}

private fun DrawScope.drawDistantMountains(w: Float, baseH: Float, form: SaiyanForm) {
    val mountainColor = when (form) {
        SaiyanForm.BASE -> Color(0xFF1565C0).copy(alpha = 0.45f)
        SaiyanForm.SUPER_SAIYAN, SaiyanForm.SUPER_SAIYAN_2, SaiyanForm.SUPER_SAIYAN_3 -> Color(0xFF4E342E).copy(alpha = 0.6f)
        SaiyanForm.SUPER_SAIYAN_GOD -> Color(0xFF4A148C).copy(alpha = 0.5f)
        SaiyanForm.SUPER_SAIYAN_BLUE -> Color(0xFF002244).copy(alpha = 0.6f)
        else -> Color(0xFF212121).copy(alpha = 0.7f)
    }

    val mountainPath = Path().apply {
        moveTo(0f, baseH)
        lineTo(w * 0.15f, baseH - 90f)
        lineTo(w * 0.35f, baseH - 40f)
        lineTo(w * 0.55f, baseH - 120f)
        lineTo(w * 0.75f, baseH - 60f)
        lineTo(w * 0.92f, baseH - 140f)
        lineTo(w, baseH - 50f)
        lineTo(w, baseH + 100f)
        lineTo(0f, baseH + 100f)
        close()
    }
    drawPath(mountainPath, color = mountainColor)
}

private fun DrawScope.drawEnergyPoles(
    pipes: List<PipeObstacle>,
    scaleX: Float,
    scaleY: Float,
    screenHeight: Float,
    form: SaiyanForm
) {
    for (pipe in pipes) {
        val px = pipe.x * scaleX
        val pw = pipe.width * scaleX
        val topH = pipe.topHeight * scaleY
        val gapH = pipe.gap * scaleY
        val bottomY = topH + gapH

        val pillarBodyColor = Color(0xFF263238)
        val pillarCapColor = Color(0xFFFFD54F)
        val energyCoreColor = form.auraInnerColor.copy(alpha = 0.85f)

        // --- TOP PILLAR ---
        // Pillar Main Body
        drawRect(
            color = pillarBodyColor,
            topLeft = Offset(px, 0f),
            size = Size(pw, topH)
        )
        // Pillar 3D shading
        drawRect(
            color = Color(0x44FFFFFF),
            topLeft = Offset(px + 4f, 0f),
            size = Size(pw * 0.25f, topH)
        )
        drawRect(
            color = Color(0x66000000),
            topLeft = Offset(px + pw * 0.75f, 0f),
            size = Size(pw * 0.25f, topH)
        )
        // Glowing Ki Energy Conduit down center of pillar
        drawRect(
            brush = Brush.verticalGradient(
                listOf(energyCoreColor, form.auraOuterColor),
                startY = 0f,
                endY = topH
            ),
            topLeft = Offset(px + pw * 0.42f, 0f),
            size = Size(pw * 0.16f, topH)
        )
        // Bottom Cap of Top Pillar
        drawRoundRect(
            color = pillarCapColor,
            topLeft = Offset(px - 5f, topH - 24f),
            size = Size(pw + 10f, 24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // --- BOTTOM PILLAR ---
        val bottomH = screenHeight - bottomY
        // Top Cap of Bottom Pillar
        drawRoundRect(
            color = pillarCapColor,
            topLeft = Offset(px - 5f, bottomY),
            size = Size(pw + 10f, 24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // Pillar Main Body
        drawRect(
            color = pillarBodyColor,
            topLeft = Offset(px, bottomY + 20f),
            size = Size(pw, bottomH)
        )
        // Shading
        drawRect(
            color = Color(0x44FFFFFF),
            topLeft = Offset(px + 4f, bottomY + 20f),
            size = Size(pw * 0.25f, bottomH)
        )
        drawRect(
            color = Color(0x66000000),
            topLeft = Offset(px + pw * 0.75f, bottomY + 20f),
            size = Size(pw * 0.25f, bottomH)
        )
        // Glowing Ki Energy Conduit
        drawRect(
            brush = Brush.verticalGradient(
                listOf(form.auraOuterColor, energyCoreColor),
                startY = bottomY + 20f,
                endY = screenHeight
            ),
            topLeft = Offset(px + pw * 0.42f, bottomY + 20f),
            size = Size(pw * 0.16f, bottomH)
        )
    }
}

private fun DrawScope.drawEnvironmentParticles(
    particles: List<KiParticle>,
    scaleX: Float,
    scaleY: Float
) {
    for (p in particles) {
        val px = p.x * scaleX
        val py = p.y * scaleY
        val pColor = Color(p.color).copy(alpha = p.alpha)
        drawCircle(
            color = pColor,
            radius = p.size * scaleX,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawArenaGround(
    w: Float,
    h: Float,
    groundTop: Float,
    groundOffset: Float,
    form: SaiyanForm
) {
    val groundHeight = h - groundTop

    // Ground base tile
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF455A64), Color(0xFF263238), Color(0xFF1A1A1A)),
            startY = groundTop,
            endY = h
        ),
        topLeft = Offset(0f, groundTop),
        size = Size(w, groundHeight)
    )

    // Top gold/stone border ridge of the martial arts arena
    drawRect(
        color = Color(0xFFFFB300),
        topLeft = Offset(0f, groundTop),
        size = Size(w, 8f)
    )

    // Scrolling arena tile seams
    val tileSize = 40f
    var startX = -groundOffset
    while (startX < w + tileSize) {
        drawLine(
            color = Color(0xFF607D8B),
            start = Offset(startX, groundTop + 8f),
            end = Offset(startX - 20f, h),
            strokeWidth = 2.5f
        )
        startX += tileSize
    }
}
