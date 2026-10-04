package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.game.LightningBolt
import com.example.model.CharacterType
import com.example.model.SaiyanForm

@Composable
fun BirdVisuals(
    character: CharacterType,
    form: SaiyanForm,
    rotation: Float,
    lightningBolts: List<LightningBolt>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura_transition")
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )
    val auraRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotate"
    )
    val wingFlap by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wing_flap"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = size.width * 0.26f
        val oozaruScale = if (form.isOozaru) 1.45f else 1.0f
        val radius = baseRadius * oozaruScale

        drawSaiyanBirdCharacter(
            centerX = centerX,
            centerY = centerY,
            radius = radius,
            character = character,
            form = form,
            rotation = rotation,
            auraPulse = auraPulse,
            auraRotate = auraRotate,
            wingFlap = wingFlap,
            lightningBolts = lightningBolts
        )
    }
}

fun DrawScope.drawSaiyanBirdCharacter(
    centerX: Float,
    centerY: Float,
    radius: Float,
    character: CharacterType,
    form: SaiyanForm,
    rotation: Float,
    auraPulse: Float,
    auraRotate: Float,
    wingFlap: Float,
    lightningBolts: List<LightningBolt>
) {
    // 1. Draw Ki Aura behind the character
    drawSaiyanAura(
        centerX = centerX,
        centerY = centerY,
        radius = radius,
        form = form,
        pulse = auraPulse,
        rotateDeg = auraRotate,
        lightningBolts = lightningBolts
    )

    // 2. Rotate character with jump/fall angle
    rotate(degrees = rotation, pivot = Offset(centerX, centerY)) {
        if (form.isOozaru) {
            // Colossal Oozaru Great Ape Monkey Mode (Bigger size, roaring ape form)
            drawOozaruGreatApe(centerX, centerY, radius, form, wingFlap)
        } else {
            // SSJ3 Long Hair or SSJ4 Wild Mane extending behind body
            if (form == SaiyanForm.SUPER_SAIYAN_3) {
                drawSSJ3LongHair(centerX, centerY, radius, form)
            } else if (form == SaiyanForm.SUPER_SAIYAN_4) {
                drawSSJ4ManeHair(centerX, centerY, radius)
            }

            // Draw Character Costume Body (or SSJ4 crimson fur body / Ultra Xeno divine white robes)
            drawCharacterCostume(centerX, centerY, radius, character, form)

            // Draw Wing with flap
            drawCharacterWing(centerX, centerY, radius, character, form, wingFlap)

            // Draw Authentic Anime Face (Skin, Eyes, Brows, Beak, Expressions)
            drawCharacterAnimeFace(centerX, centerY, radius, character, form)

            // Draw Character Saiyan Hair
            drawCharacterSaiyanHair(centerX, centerY, radius, character, form)

            // Ultra Ego Hakai Orbs
            if (form == SaiyanForm.ULTRA_EGO || form == SaiyanForm.MASTERED_ULTRA_EGO) {
                drawHakaiOrbs(centerX, centerY, radius, auraRotate)
            }
        }
    }
}

private fun DrawScope.drawSaiyanAura(
    centerX: Float,
    centerY: Float,
    radius: Float,
    form: SaiyanForm,
    pulse: Float,
    rotateDeg: Float,
    lightningBolts: List<LightningBolt>
) {
    if (form == SaiyanForm.BASE) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x33FFFFFF), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = radius * 1.4f * pulse
            ),
            radius = radius * 1.4f * pulse,
            center = Offset(centerX, centerY)
        )
        return
    }

    val outerRadius = radius * 2.3f * pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                form.auraInnerColor.copy(alpha = 0.88f),
                form.auraOuterColor.copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = outerRadius
        ),
        radius = outerRadius,
        center = Offset(centerX, centerY)
    )

    // Rotating Ki flame petals
    val petalCount = if (form.isGodly || form.isOozaru) 10 else 6
    val angleStep = 360f / petalCount
    for (i in 0 until petalCount) {
        val angle = Math.toRadians((i * angleStep + rotateDeg).toDouble())
        val px = centerX + (kotlin.math.cos(angle) * radius * 1.3f * pulse).toFloat()
        val py = centerY + (kotlin.math.sin(angle) * radius * 1.3f * pulse).toFloat()
        drawCircle(
            color = form.auraInnerColor.copy(alpha = 0.65f),
            radius = radius * 0.42f,
            center = Offset(px, py)
        )
    }

    // Lightning arcs
    if (form.hasLightning) {
        val lightningColor = when (form) {
            SaiyanForm.MASTERED_ULTRA_EGO -> Color(0xFFE040FB)
            SaiyanForm.MASTERED_ULTRA_INSTINCT, SaiyanForm.ULTRA_XENO -> Color(0xFFFFFFFF)
            SaiyanForm.BLACK_OOZARU -> Color(0xFFFF1744)
            SaiyanForm.WHITE_OOZARU -> Color(0xFFB388FF)
            else -> Color(0xFF80D8FF)
        }
        for (bolt in lightningBolts) {
            if (bolt.points.size > 1) {
                val path = Path()
                path.moveTo(bolt.points[0].x, bolt.points[0].y)
                for (j in 1 until bolt.points.size) {
                    path.lineTo(bolt.points[j].x, bolt.points[j].y)
                }
                drawPath(
                    path = path,
                    color = lightningColor.copy(alpha = bolt.alpha),
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

private fun DrawScope.drawCharacterCostume(
    centerX: Float,
    centerY: Float,
    radius: Float,
    character: CharacterType,
    form: SaiyanForm
) {
    if (form == SaiyanForm.SUPER_SAIYAN_4) {
        // SSJ4: Crimson beast body fur!
        drawCircle(color = Color(0xFF111111), radius = radius + 2.5f, center = Offset(centerX, centerY))
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFD50000), Color(0xFFB71C1C)),
                center = Offset(centerX, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )
        // Muscular chest outline
        drawCircle(color = Color(0xFFFFCC80), radius = radius * 0.35f, center = Offset(centerX - radius * 0.1f, centerY + radius * 0.1f))
        return
    }

    if (form == SaiyanForm.ULTRA_XENO) {
        // Ultra Xeno: Divine White Time-Patroller Robes
        drawCircle(color = Color(0xFF111111), radius = radius + 2.5f, center = Offset(centerX, centerY))
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFFFFF), Color(0xFFCFD8DC)),
                center = Offset(centerX, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )
        // Cyan sash & gold trim
        drawRect(color = Color(0xFF00E5FF), topLeft = Offset(centerX - radius * 0.6f, centerY + radius * 0.3f), size = Size(radius * 1.2f, radius * 0.22f))
        drawCircle(color = Color(0xFFFFD700), radius = radius * 0.15f, center = Offset(centerX, centerY + radius * 0.42f))
        return
    }

    // Base body circle outline
    drawCircle(
        color = Color(0xFF111111),
        radius = radius + 2.5f,
        center = Offset(centerX, centerY)
    )

    // Base body fill
    val bodyBrush = Brush.radialGradient(
        colors = listOf(
            character.suitColor,
            character.suitColor.copy(
                red = character.suitColor.red * 0.72f,
                green = character.suitColor.green * 0.72f,
                blue = character.suitColor.blue * 0.72f
            )
        ),
        center = Offset(centerX - radius * 0.3f, centerY - radius * 0.2f),
        radius = radius
    )
    drawCircle(brush = bodyBrush, radius = radius, center = Offset(centerX, centerY))

    if (character == CharacterType.GOKU) {
        // Goku's dark blue undershirt V-neck
        val vNeck = Path().apply {
            moveTo(centerX - radius * 0.35f, centerY - radius * 0.25f)
            lineTo(centerX + radius * 0.25f, centerY - radius * 0.25f)
            lineTo(centerX - radius * 0.05f, centerY + radius * 0.4f)
            close()
        }
        drawPath(vNeck, color = Color(0xFF0D47A1))

        // Blue belt knot accent
        drawCircle(
            color = Color(0xFF0D47A1),
            radius = radius * 0.22f,
            center = Offset(centerX - radius * 0.1f, centerY + radius * 0.75f)
        )

        // Goku's Kame/Go white circle chest badge
        val badgeCenter = Offset(centerX - radius * 0.35f, centerY + radius * 0.22f)
        drawCircle(color = Color.White, radius = radius * 0.3f, center = badgeCenter)
        drawCircle(color = Color(0xFF212121), radius = radius * 0.3f, center = badgeCenter, style = Stroke(width = 1.8f))
        drawCircle(color = Color(0xFFD32F2F), radius = radius * 0.12f, center = badgeCenter)
    } else {
        // Vegeta's Saiyan Combat Armor Chestplate
        val armorPath = Path().apply {
            moveTo(centerX - radius * 0.65f, centerY - radius * 0.15f)
            lineTo(centerX + radius * 0.35f, centerY - radius * 0.15f)
            lineTo(centerX + radius * 0.25f, centerY + radius * 0.85f)
            lineTo(centerX - radius * 0.55f, centerY + radius * 0.85f)
            close()
        }
        drawPath(armorPath, color = Color(0xFFFFFFFF))
        drawPath(armorPath, color = Color(0xFF212121), style = Stroke(width = 2f))

        // Gold armor straps
        val goldStrap = Path().apply {
            moveTo(centerX - radius * 0.55f, centerY - radius * 0.1f)
            lineTo(centerX - radius * 0.25f, centerY - radius * 0.1f)
            lineTo(centerX - radius * 0.25f, centerY + radius * 0.35f)
            lineTo(centerX - radius * 0.55f, centerY + radius * 0.35f)
            close()
        }
        drawPath(goldStrap, color = Color(0xFFFFD54F))
        drawPath(goldStrap, color = Color(0xFF111111), style = Stroke(width = 1.2f))
    }
}

private fun DrawScope.drawCharacterWing(
    centerX: Float,
    centerY: Float,
    radius: Float,
    character: CharacterType,
    form: SaiyanForm,
    wingAngle: Float
) {
    val wingColor = when (form) {
        SaiyanForm.SUPER_SAIYAN_4 -> Color(0xFFD50000)
        SaiyanForm.ULTRA_XENO -> Color(0xFFFFFFFF)
        else -> character.accentColor
    }

    rotate(degrees = wingAngle, pivot = Offset(centerX - radius * 0.45f, centerY)) {
        val wingPath = Path().apply {
            moveTo(centerX - radius * 0.45f, centerY - radius * 0.1f)
            cubicTo(
                centerX - radius * 0.95f, centerY - radius * 0.5f,
                centerX - radius * 1.05f, centerY + radius * 0.7f,
                centerX - radius * 0.25f, centerY + radius * 0.5f
            )
            close()
        }
        drawPath(wingPath, color = Color(0xFF111111), style = Stroke(width = 2.8f, join = StrokeJoin.Round))
        drawPath(wingPath, color = wingColor)
    }
}

private fun DrawScope.drawCharacterAnimeFace(
    centerX: Float,
    centerY: Float,
    radius: Float,
    character: CharacterType,
    form: SaiyanForm
) {
    val isVegeta = character == CharacterType.VEGETA

    // 1. Anime Skin Tone
    val skinColor = when {
        form == SaiyanForm.ULTRA_INSTINCT || form == SaiyanForm.MASTERED_ULTRA_INSTINCT -> Color(0xFFEDE7F6)
        form == SaiyanForm.ULTRA_XENO -> Color(0xFFF3E5F5)
        form == SaiyanForm.SUPER_SAIYAN_4 -> Color(0xFFFFD180)
        else -> Color(0xFFFFCC80)
    }

    val skinPath = Path().apply {
        moveTo(centerX - radius * 0.4f, centerY - radius * 0.5f)
        lineTo(centerX + radius * 0.55f, centerY - radius * 0.45f)
        lineTo(centerX + radius * 0.85f, centerY + radius * 0.05f)
        lineTo(centerX + radius * 0.65f, centerY + radius * 0.55f)
        lineTo(centerX - radius * 0.15f, centerY + radius * 0.65f)
        lineTo(centerX - radius * 0.45f, centerY + radius * 0.25f)
        close()
    }
    drawPath(skinPath, color = skinColor)

    // 2. Eye rendering
    val eyeW = radius * 0.48f
    val eyeH = radius * 0.36f
    val eyeCenterX = centerX + radius * 0.22f
    val eyeCenterY = centerY - radius * 0.08f

    // SSJ4 Red Eye Contour Shadow Mask
    if (form == SaiyanForm.SUPER_SAIYAN_4) {
        val maskPath = Path().apply {
            moveTo(eyeCenterX - eyeW * 0.65f, eyeCenterY - eyeH * 0.55f)
            lineTo(eyeCenterX + eyeW * 0.65f, eyeCenterY - eyeH * 0.55f)
            lineTo(eyeCenterX + eyeW * 0.65f, eyeCenterY + eyeH * 0.65f)
            lineTo(eyeCenterX - eyeW * 0.65f, eyeCenterY + eyeH * 0.65f)
            close()
        }
        drawPath(maskPath, color = Color(0xFFD50000))
    }

    val eyeWhite = Path().apply {
        moveTo(eyeCenterX - eyeW * 0.55f, eyeCenterY - eyeH * 0.35f)
        lineTo(eyeCenterX + eyeW * 0.55f, eyeCenterY - eyeH * 0.2f)
        lineTo(eyeCenterX + eyeW * 0.35f, eyeCenterY + eyeH * 0.45f)
        lineTo(eyeCenterX - eyeW * 0.45f, eyeCenterY + eyeH * 0.35f)
        close()
    }
    drawPath(eyeWhite, color = Color.White)
    drawPath(eyeWhite, color = Color(0xFF111111), style = Stroke(width = 2.4f, join = StrokeJoin.Round))

    // Iris color by transformation
    val irisColor = when (form) {
        SaiyanForm.BASE -> Color(0xFF1A1A1A)
        SaiyanForm.SUPER_SAIYAN, SaiyanForm.SUPER_SAIYAN_2, SaiyanForm.SUPER_SAIYAN_3 -> Color(0xFF00E676)
        SaiyanForm.SUPER_SAIYAN_GOD -> Color(0xFFFF1744)
        SaiyanForm.SUPER_SAIYAN_BLUE -> Color(0xFF00E5FF)
        SaiyanForm.ULTRA_INSTINCT, SaiyanForm.MASTERED_ULTRA_INSTINCT -> Color(0xFFE0E0E0)
        SaiyanForm.ULTRA_EGO, SaiyanForm.MASTERED_ULTRA_EGO -> Color(0xFFE040FB)
        SaiyanForm.SUPER_SAIYAN_4 -> Color(0xFFFFD600) // Fierce amber/yellow SSJ4 iris!
        SaiyanForm.ULTRA_XENO -> Color(0xFFB3E5FC) // Celestial silver-cyan!
        else -> Color(0xFF1A1A1A)
    }

    val irisRadius = eyeH * 0.45f
    val irisCenter = Offset(eyeCenterX + eyeW * 0.05f, eyeCenterY + eyeH * 0.05f)
    drawCircle(color = irisColor, radius = irisRadius, center = irisCenter)
    drawCircle(color = Color(0xFF111111), radius = irisRadius * 0.55f, center = irisCenter)
    drawCircle(color = Color.White, radius = irisRadius * 0.26f, center = Offset(irisCenter.x + irisRadius * 0.3f, irisCenter.y - irisRadius * 0.3f))

    // 3. Eyebrow
    val browColor = form.primaryHairColor
    val browPath = Path().apply {
        if (isVegeta) {
            moveTo(eyeCenterX - eyeW * 0.65f, eyeCenterY - eyeH * 0.15f)
            lineTo(eyeCenterX + eyeW * 0.68f, eyeCenterY - eyeH * 0.65f)
            lineTo(eyeCenterX + eyeW * 0.62f, eyeCenterY - eyeH * 0.32f)
            lineTo(eyeCenterX - eyeW * 0.55f, eyeCenterY - eyeH * 0.05f)
        } else {
            moveTo(eyeCenterX - eyeW * 0.6f, eyeCenterY - eyeH * 0.1f)
            lineTo(eyeCenterX + eyeW * 0.62f, eyeCenterY - eyeH * 0.5f)
            lineTo(eyeCenterX + eyeW * 0.55f, eyeCenterY - eyeH * 0.25f)
            lineTo(eyeCenterX - eyeW * 0.5f, eyeCenterY - eyeH * 0.02f)
        }
        close()
    }
    drawPath(browPath, color = Color(0xFF111111), style = Stroke(width = 2.4f, join = StrokeJoin.Round))
    drawPath(browPath, color = browColor)

    // 4. Stylized Anime Beak & Mouth Expression
    drawAnimeBeakAndMouth(centerX, centerY, radius, isVegeta)
}

private fun DrawScope.drawAnimeBeakAndMouth(
    centerX: Float,
    centerY: Float,
    radius: Float,
    isVegeta: Boolean
) {
    val beakX = centerX + radius * 0.72f
    val beakY = centerY + radius * 0.05f

    val beakPath = Path().apply {
        moveTo(beakX - radius * 0.05f, beakY - radius * 0.12f)
        lineTo(beakX + radius * 0.72f, beakY + radius * 0.08f)
        lineTo(beakX - radius * 0.1f, beakY + radius * 0.3f)
        close()
    }

    drawPath(beakPath, color = Color(0xFF111111), style = Stroke(width = 2.5f, join = StrokeJoin.Round))
    drawPath(
        beakPath,
        brush = Brush.linearGradient(
            listOf(Color(0xFFFFB300), Color(0xFFFF6D00)),
            start = Offset(beakX, beakY),
            end = Offset(beakX + radius * 0.7f, beakY)
        )
    )

    val smirkPath = Path().apply {
        moveTo(beakX - radius * 0.05f, beakY + radius * 0.05f)
        if (isVegeta) {
            lineTo(beakX + radius * 0.35f, beakY + radius * 0.08f)
            lineTo(beakX + radius * 0.65f, beakY + radius * 0.08f)
        } else {
            lineTo(beakX + radius * 0.35f, beakY + radius * 0.04f)
            lineTo(beakX + radius * 0.65f, beakY + radius * 0.08f)
        }
    }
    drawPath(smirkPath, color = Color(0xFFBF360C), style = Stroke(width = 2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawCharacterSaiyanHair(
    centerX: Float,
    centerY: Float,
    radius: Float,
    character: CharacterType,
    form: SaiyanForm
) {
    val primary = form.primaryHairColor
    val secondary = form.secondaryHairColor
    val hairPath = Path()

    if (character == CharacterType.GOKU) {
        val topY = centerY - radius * 0.85f

        // Spike 1: Left wide curved spike
        hairPath.moveTo(centerX - radius * 0.95f, centerY - radius * 0.05f)
        hairPath.cubicTo(
            centerX - radius * 1.6f, centerY - radius * 0.35f,
            centerX - radius * 1.7f, centerY - radius * 1.15f,
            centerX - radius * 0.85f, topY - radius * 0.55f
        )
        // Spike 2: Giant top-left curved spike
        hairPath.cubicTo(
            centerX - radius * 1.35f, topY - radius * 1.55f,
            centerX - radius * 0.45f, topY - radius * 1.75f,
            centerX - radius * 0.1f, topY - radius * 1.2f
        )
        // Spike 3: Crown top-right spike
        hairPath.cubicTo(
            centerX + radius * 0.35f, topY - radius * 1.65f,
            centerX + radius * 1.05f, topY - radius * 1.3f,
            centerX + radius * 0.65f, topY - radius * 0.55f
        )
        // Spike 4: Forehead outer spike
        hairPath.cubicTo(
            centerX + radius * 0.85f, topY - radius * 0.2f,
            centerX + radius * 0.55f, centerY - radius * 0.2f,
            centerX + radius * 0.35f, centerY - radius * 0.45f
        )
        hairPath.lineTo(centerX + radius * 0.25f, centerY - radius * 0.25f)
        hairPath.lineTo(centerX + radius * 0.15f, centerY - radius * 0.5f)
        hairPath.lineTo(centerX - radius * 0.05f, centerY - radius * 0.3f)
        hairPath.lineTo(centerX - radius * 0.25f, centerY - radius * 0.55f)
        hairPath.lineTo(centerX - radius * 0.65f, centerY - radius * 0.55f)
        hairPath.close()

    } else {
        // VEGETA: Iconic Upright Flame Crown with deep Widow's Peak hairline
        val topY = centerY - radius * 0.85f

        hairPath.moveTo(centerX - radius * 0.9f, centerY - radius * 0.05f)
        hairPath.cubicTo(
            centerX - radius * 1.4f, centerY - radius * 0.65f,
            centerX - radius * 1.15f, topY - radius * 1.1f,
            centerX - radius * 0.65f, topY - radius * 0.95f
        )
        hairPath.cubicTo(
            centerX - radius * 0.9f, topY - radius * 1.85f,
            centerX - radius * 0.15f, topY - radius * 2.05f,
            centerX + radius * 0.18f, topY - radius * 1.4f
        )
        hairPath.cubicTo(
            centerX + radius * 0.55f, topY - radius * 1.75f,
            centerX + radius * 0.95f, topY - radius * 1.0f,
            centerX + radius * 0.55f, topY - radius * 0.35f
        )
        // Widow's peak tip!
        hairPath.lineTo(centerX + radius * 0.35f, centerY - radius * 0.4f)
        hairPath.lineTo(centerX + radius * 0.12f, centerY - radius * 0.62f)
        hairPath.lineTo(centerX - radius * 0.15f, centerY - radius * 0.45f)
        hairPath.lineTo(centerX - radius * 0.45f, centerY - radius * 0.4f)
        hairPath.close()
    }

    drawPath(path = hairPath, color = Color(0xFF111111), style = Stroke(width = 3.5f, join = StrokeJoin.Round))
    drawPath(
        path = hairPath,
        brush = Brush.linearGradient(
            listOf(primary, secondary),
            start = Offset(centerX, centerY - radius * 1.5f),
            end = Offset(centerX, centerY)
        )
    )
}

/**
 * Colossal Oozaru Great Ape Monkey Form
 * Rendered when in Oozaru modes: Black Oozaru, Golden Oozaru, White Oozaru
 */
private fun DrawScope.drawOozaruGreatApe(
    centerX: Float,
    centerY: Float,
    radius: Float,
    form: SaiyanForm,
    wingFlap: Float
) {
    val furColor = form.primaryHairColor
    val shadeColor = form.secondaryHairColor
    val snoutColor = when (form) {
        SaiyanForm.BLACK_OOZARU -> Color(0xFF37474F)
        SaiyanForm.GOLDEN_OOZARU -> Color(0xFFE65100)
        else -> Color(0xFF78909C) // White Oozaru
    }
    val eyeColor = when (form) {
        SaiyanForm.WHITE_OOZARU -> Color(0xFF7C4DFF)
        else -> Color(0xFFFF1744) // Demon red eyes
    }

    // 1. Simian Ape Ears
    // Left ear
    drawCircle(color = Color(0xFF111111), radius = radius * 0.38f, center = Offset(centerX - radius * 0.85f, centerY - radius * 0.35f))
    drawCircle(color = furColor, radius = radius * 0.35f, center = Offset(centerX - radius * 0.85f, centerY - radius * 0.35f))
    drawCircle(color = snoutColor.copy(alpha = 0.8f), radius = radius * 0.2f, center = Offset(centerX - radius * 0.85f, centerY - radius * 0.35f))

    // Right/Top-back ear
    drawCircle(color = Color(0xFF111111), radius = radius * 0.34f, center = Offset(centerX - radius * 0.35f, centerY - radius * 0.95f))
    drawCircle(color = furColor, radius = radius * 0.31f, center = Offset(centerX - radius * 0.35f, centerY - radius * 0.95f))

    // 2. Colossal Ape Fur Body
    drawCircle(color = Color(0xFF111111), radius = radius + 3f, center = Offset(centerX, centerY))
    drawCircle(
        brush = Brush.radialGradient(
            listOf(furColor, shadeColor),
            center = Offset(centerX - radius * 0.2f, centerY - radius * 0.2f),
            radius = radius
        ),
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Wild fur spikes around perimeter of ape head
    val furTufts = 8
    val angleStep = 360f / furTufts
    for (i in 0 until furTufts) {
        val ang = Math.toRadians((i * angleStep).toDouble())
        val tx = centerX + (kotlin.math.cos(ang) * radius * 0.92f).toFloat()
        val ty = centerY + (kotlin.math.sin(ang) * radius * 0.92f).toFloat()
        drawCircle(color = furColor, radius = radius * 0.22f, center = Offset(tx, ty))
    }

    // 3. Ape Muscular Wing / Fur Arm
    rotate(degrees = wingFlap * 0.85f, pivot = Offset(centerX - radius * 0.45f, centerY)) {
        val apeArm = Path().apply {
            moveTo(centerX - radius * 0.45f, centerY - radius * 0.1f)
            cubicTo(
                centerX - radius * 1.25f, centerY - radius * 0.4f,
                centerX - radius * 1.35f, centerY + radius * 0.9f,
                centerX - radius * 0.2f, centerY + radius * 0.6f
            )
            close()
        }
        drawPath(apeArm, color = Color(0xFF111111), style = Stroke(width = 3.5f, join = StrokeJoin.Round))
        drawPath(apeArm, color = furColor)
    }

    // 4. Heavy Menacing Ape Brow Ridge
    val browPath = Path().apply {
        moveTo(centerX + radius * 0.05f, centerY - radius * 0.45f)
        lineTo(centerX + radius * 0.85f, centerY - radius * 0.25f)
        lineTo(centerX + radius * 0.75f, centerY - radius * 0.05f)
        lineTo(centerX + radius * 0.05f, centerY - radius * 0.25f)
        close()
    }
    drawPath(browPath, color = Color(0xFF111111), style = Stroke(width = 3f, join = StrokeJoin.Round))
    drawPath(browPath, color = snoutColor)

    // 5. Glowing Demonic Beast Ape Eye
    val eyeCenterX = centerX + radius * 0.42f
    val eyeCenterY = centerY - radius * 0.16f
    drawCircle(color = Color(0xFF000000), radius = radius * 0.24f, center = Offset(eyeCenterX, eyeCenterY))
    drawCircle(
        brush = Brush.radialGradient(
            listOf(eyeColor, eyeColor.copy(alpha = 0.5f)),
            center = Offset(eyeCenterX, eyeCenterY),
            radius = radius * 0.22f
        ),
        radius = radius * 0.2f,
        center = Offset(eyeCenterX, eyeCenterY)
    )
    drawOval(
        color = Color(0xFF111111),
        topLeft = Offset(eyeCenterX - radius * 0.04f, eyeCenterY - radius * 0.15f),
        size = Size(radius * 0.08f, radius * 0.3f)
    )
    drawCircle(color = Color.White, radius = radius * 0.045f, center = Offset(eyeCenterX + radius * 0.07f, eyeCenterY - radius * 0.07f))

    // 6. Giant Ape Snout / Muzzle
    val snoutPath = Path().apply {
        moveTo(centerX + radius * 0.2f, centerY)
        lineTo(centerX + radius * 0.95f, centerY - radius * 0.05f)
        lineTo(centerX + radius * 1.05f, centerY + radius * 0.35f)
        lineTo(centerX + radius * 0.15f, centerY + radius * 0.45f)
        close()
    }
    drawPath(snoutPath, color = Color(0xFF111111), style = Stroke(width = 3f, join = StrokeJoin.Round))
    drawPath(snoutPath, color = snoutColor)

    // Beast nostrils
    drawCircle(color = Color(0xFF111111), radius = radius * 0.06f, center = Offset(centerX + radius * 0.88f, centerY + radius * 0.12f))
    drawCircle(color = Color(0xFF111111), radius = radius * 0.06f, center = Offset(centerX + radius * 0.72f, centerY + radius * 0.12f))

    // 7. Sharp Predator Fangs
    val topFang = Path().apply {
        moveTo(centerX + radius * 0.65f, centerY + radius * 0.35f)
        lineTo(centerX + radius * 0.8f, centerY + radius * 0.35f)
        lineTo(centerX + radius * 0.72f, centerY + radius * 0.62f)
        close()
    }
    drawPath(topFang, color = Color(0xFFFFFFFF))
    drawPath(topFang, color = Color(0xFF111111), style = Stroke(width = 1.5f))

    val bottomFang = Path().apply {
        moveTo(centerX + radius * 0.42f, centerY + radius * 0.55f)
        lineTo(centerX + radius * 0.56f, centerY + radius * 0.55f)
        lineTo(centerX + radius * 0.49f, centerY + radius * 0.25f)
        close()
    }
    drawPath(bottomFang, color = Color(0xFFFFFFFF))
    drawPath(bottomFang, color = Color(0xFF111111), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawSSJ4ManeHair(
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val manePath = Path().apply {
        moveTo(centerX - radius * 0.8f, centerY - radius * 0.5f)
        cubicTo(
            centerX - radius * 1.7f, centerY - radius * 1.1f,
            centerX - radius * 1.9f, centerY + radius * 0.9f,
            centerX - radius * 1.1f, centerY + radius * 1.4f
        )
        lineTo(centerX - radius * 0.4f, centerY + radius * 0.9f)
        close()
    }
    drawPath(manePath, color = Color(0xFF111111), style = Stroke(width = 3.5f, join = StrokeJoin.Round))
    drawPath(manePath, color = Color(0xFF1E1E1E))
}

private fun DrawScope.drawSSJ3LongHair(
    centerX: Float,
    centerY: Float,
    radius: Float,
    form: SaiyanForm
) {
    val longHair = Path().apply {
        moveTo(centerX - radius * 0.6f, centerY - radius * 0.7f)
        cubicTo(
            centerX - radius * 1.5f, centerY - radius * 1.2f,
            centerX - radius * 2.2f, centerY + radius * 0.2f,
            centerX - radius * 1.8f, centerY + radius * 1.8f
        )
        lineTo(centerX - radius * 1.4f, centerY + radius * 1.7f)
        cubicTo(
            centerX - radius * 1.9f, centerY + radius * 0.5f,
            centerX - radius * 1.3f, centerY - radius * 0.2f,
            centerX - radius * 0.2f, centerY + radius * 0.4f
        )
        close()
    }

    drawPath(longHair, color = Color(0xFF111111), style = Stroke(width = 3.8f, join = StrokeJoin.Round))
    drawPath(
        longHair,
        brush = Brush.linearGradient(
            listOf(form.primaryHairColor, form.secondaryHairColor),
            start = Offset(centerX - radius, centerY - radius),
            end = Offset(centerX - radius * 2f, centerY + radius * 1.9f)
        )
    )
}

private fun DrawScope.drawHakaiOrbs(
    centerX: Float,
    centerY: Float,
    radius: Float,
    rotateDeg: Float
) {
    val orbCount = 3
    for (i in 0 until orbCount) {
        val angle = Math.toRadians((i * (360.0 / orbCount) + rotateDeg * 1.6).toDouble())
        val dist = radius * 1.8f
        val ox = centerX + (kotlin.math.cos(angle) * dist).toFloat()
        val oy = centerY + (kotlin.math.sin(angle) * dist).toFloat()

        drawCircle(color = Color(0xFF4A148C), radius = radius * 0.26f, center = Offset(ox, oy))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE040FB), Color(0xFF7B1FA2), Color.Transparent),
                center = Offset(ox, oy),
                radius = radius * 0.24f
            ),
            radius = radius * 0.24f,
            center = Offset(ox, oy)
        )
    }
}
