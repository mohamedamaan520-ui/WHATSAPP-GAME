package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameEngine
import com.example.model.*
import com.example.ui.theme.*
import com.example.whatsapp.WhatsAppHelper
import kotlin.math.*

@Composable
fun VirtualJoystick(
    joystickSize: Dp = 130.dp,
    knobColor: Color = NeonCyan,
    baseColor: Color = CyberDarkCard,
    label: String = "MOVE",
    onValueChange: (vx: Float, vy: Float, angle: Float, isEngaged: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadiusPx = 90f

    Box(
        modifier = modifier
            .size(joystickSize)
            .clip(CircleShape)
            .background(baseColor.copy(alpha = 0.65f))
            .border(2.dp, knobColor.copy(alpha = 0.5f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(joystickSize.toPx() / 2f, joystickSize.toPx() / 2f)
                        val dragVector = offset - center
                        val dist = dragVector.getDistance()
                        val clamped = if (dist > maxRadiusPx) {
                            dragVector * (maxRadiusPx / dist)
                        } else {
                            dragVector
                        }
                        knobOffset = clamped
                        val normX = clamped.x / maxRadiusPx
                        val normY = clamped.y / maxRadiusPx
                        val angle = atan2(normY, normX)
                        onValueChange(normX, normY, angle, true)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clamped = if (dist > maxRadiusPx) {
                            newOffset * (maxRadiusPx / dist)
                        } else {
                            newOffset
                        }
                        knobOffset = clamped
                        val normX = clamped.x / maxRadiusPx
                        val normY = clamped.y / maxRadiusPx
                        val angle = atan2(normY, normX)
                        onValueChange(normX, normY, angle, true)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onValueChange(0f, 0f, 0f, false)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onValueChange(0f, 0f, 0f, false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Inner crosshair guide
            drawLine(
                color = knobColor.copy(alpha = 0.2f),
                start = Offset(center.x - 30f, center.y),
                end = Offset(center.x + 30f, center.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = knobColor.copy(alpha = 0.2f),
                start = Offset(center.x, center.y - 30f),
                end = Offset(center.x, center.y + 30f),
                strokeWidth = 1.5f
            )

            // Dynamic Knob
            val knobCenter = center + knobOffset
            drawCircle(
                color = knobColor.copy(alpha = 0.35f),
                radius = 32f,
                center = knobCenter
            )
            drawCircle(
                color = knobColor,
                radius = 24f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = knobCenter
            )
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = knobColor.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
        )
    }
}

@Composable
fun GameCanvasView(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Scaling factor from game world to viewport
        val scaleX = canvasWidth / engine.worldWidth
        val scaleY = canvasHeight / engine.worldHeight
        val scale = min(scaleX, scaleY)

        val offsetX = (canvasWidth - engine.worldWidth * scale) / 2f
        val offsetY = (canvasHeight - engine.worldHeight * scale) / 2f

        // Draw Cyber Grid Background
        drawCyberGrid(offsetX, offsetY, engine.worldWidth * scale, engine.worldHeight * scale, scale)

        // Draw Obstacles
        for (obs in engine.obstacles) {
            val ox = offsetX + obs.x * scale
            val oy = offsetY + obs.y * scale
            val ow = obs.width * scale
            val oh = obs.height * scale

            if (obs.isDestructible) {
                // Energy Crate
                drawRoundRect(
                    color = CyberDarkCard,
                    topLeft = Offset(ox, oy),
                    size = Size(ow, oh),
                    cornerRadius = CornerRadius(6f * scale, 6f * scale)
                )
                drawRoundRect(
                    color = obs.color,
                    topLeft = Offset(ox, oy),
                    size = Size(ow, oh),
                    cornerRadius = CornerRadius(6f * scale, 6f * scale),
                    style = Stroke(width = 2.5f * scale)
                )
                // Diagonal warning lines
                drawLine(
                    color = obs.color.copy(alpha = 0.5f),
                    start = Offset(ox, oy),
                    end = Offset(ox + ow, oy + oh),
                    strokeWidth = 2f * scale
                )
                // Crate health bar
                if (obs.health < obs.maxHealth) {
                    val hpRatio = (obs.health / obs.maxHealth).coerceIn(0f, 1f)
                    drawRect(
                        color = Color.DarkGray,
                        topLeft = Offset(ox, oy - 8f * scale),
                        size = Size(ow, 4f * scale)
                    )
                    drawRect(
                        color = NeonYellow,
                        topLeft = Offset(ox, oy - 8f * scale),
                        size = Size(ow * hpRatio, 4f * scale)
                    )
                }
            } else {
                // Cyber Wall / Bunker
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(CyberDarkCard, obs.color.copy(alpha = 0.25f), CyberDarkCard),
                        start = Offset(ox, oy),
                        end = Offset(ox + ow, oy + oh)
                    ),
                    topLeft = Offset(ox, oy),
                    size = Size(ow, oh),
                    cornerRadius = CornerRadius(4f * scale, 4f * scale)
                )
                drawRoundRect(
                    color = obs.color,
                    topLeft = Offset(ox, oy),
                    size = Size(ow, oh),
                    cornerRadius = CornerRadius(4f * scale, 4f * scale),
                    style = Stroke(width = 2f * scale)
                )
            }
        }

        // Draw Power-ups
        for (pu in engine.powerUps) {
            val px = offsetX + pu.x * scale
            val py = offsetY + pu.y * scale
            val pr = pu.radius * scale

            val puColor = when (pu.type) {
                PowerUpType.HEALTH_PACK -> HealthGreen
                PowerUpType.SHIELD_BOOST -> ShieldBlue
                PowerUpType.QUAD_DAMAGE -> NeonOrange
                PowerUpType.SPEED_OVERDRIVE -> NeonGreen
                PowerUpType.NUKE_BOMB -> NeonRed
            }

            // Pulsing glow
            drawCircle(
                color = puColor.copy(alpha = 0.35f),
                radius = pr * 1.5f,
                center = Offset(px, py)
            )
            drawCircle(
                color = puColor,
                radius = pr,
                center = Offset(px, py),
                style = Stroke(width = 2.5f * scale)
            )
            drawCircle(
                color = Color.White,
                radius = pr * 0.45f,
                center = Offset(px, py)
            )
        }

        // Draw Bullets & Projectiles
        for (b in engine.bullets) {
            val bx = offsetX + b.x * scale
            val by = offsetY + b.y * scale
            val br = b.radius * scale

            if (b.isRocket) {
                // Rocket shape with trail
                val angle = atan2(b.vy, b.vx) * (180f / PI.toFloat())
                rotate(degrees = angle, pivot = Offset(bx, by)) {
                    drawOval(
                        color = NeonRed,
                        topLeft = Offset(bx - br * 2f, by - br),
                        size = Size(br * 4f, br * 2f)
                    )
                    drawCircle(
                        color = NeonYellow,
                        radius = br * 0.8f,
                        center = Offset(bx - br * 1.5f, by)
                    )
                }
            } else if (b.isPiercing) {
                // Railgun laser beam streak
                val angle = atan2(b.vy, b.vx) * (180f / PI.toFloat())
                rotate(degrees = angle, pivot = Offset(bx, by)) {
                    drawRoundRect(
                        color = NeonPurple,
                        topLeft = Offset(bx - br * 4f, by - br * 0.8f),
                        size = Size(br * 8f, br * 1.6f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(bx - br * 2f, by - br * 0.4f),
                        size = Size(br * 4f, br * 0.8f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            } else {
                // Plasma Slug / Laser
                drawCircle(
                    color = b.color.copy(alpha = 0.5f),
                    radius = br * 1.6f,
                    center = Offset(bx, by)
                )
                drawCircle(
                    color = b.color,
                    radius = br,
                    center = Offset(bx, by)
                )
                drawCircle(
                    color = Color.White,
                    radius = br * 0.4f,
                    center = Offset(bx, by)
                )
            }
        }

        // Draw Particles
        for (p in engine.particles) {
            val px = offsetX + p.x * scale
            val py = offsetY + p.y * scale
            val pr = p.radius * scale
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = pr,
                center = Offset(px, py)
            )
        }

        // Draw Players
        for (player in engine.players) {
            if (!player.isAlive) continue

            val px = offsetX + player.x * scale
            val py = offsetY + player.y * scale
            val pr = (if (player.isBotBoss) 30f else 18f) * scale

            // Power-up visual aura (Quad damage / Speed boost)
            if (player.quadDamageTimer > 0f) {
                drawCircle(
                    color = NeonOrange.copy(alpha = 0.35f),
                    radius = pr * 1.8f,
                    center = Offset(px, py)
                )
            }
            if (player.speedBoostTimer > 0f) {
                drawCircle(
                    color = NeonGreen.copy(alpha = 0.3f),
                    radius = pr * 1.6f,
                    center = Offset(px, py)
                )
            }

            // Shield bubble
            if (player.shield > 0f) {
                val shieldAlpha = (player.shield / player.maxShield).coerceIn(0.2f, 0.7f)
                drawCircle(
                    color = ShieldBlue.copy(alpha = shieldAlpha * 0.3f),
                    radius = pr * 1.35f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = ShieldBlue.copy(alpha = shieldAlpha),
                    radius = pr * 1.35f,
                    center = Offset(px, py),
                    style = Stroke(width = 2f * scale)
                )
            }

            // Tank / Combatant Base Hull
            drawCircle(
                color = CyberDarkSurface,
                radius = pr,
                center = Offset(px, py)
            )
            drawCircle(
                color = player.avatarColor,
                radius = pr,
                center = Offset(px, py),
                style = Stroke(width = 3.5f * scale)
            )

            // Rotating Turret Barrel & Laser Sight
            val turretAngleDeg = player.aimAngle * (180f / PI.toFloat())
            rotate(degrees = turretAngleDeg, pivot = Offset(px, py)) {
                // Laser targeting sight
                if (player.isLocal) {
                    drawLine(
                        color = player.avatarColor.copy(alpha = 0.4f),
                        start = Offset(px + pr, py),
                        end = Offset(px + 180f * scale, py),
                        strokeWidth = 1f * scale
                    )
                }

                // Turret barrel
                drawRoundRect(
                    color = player.avatarColor,
                    topLeft = Offset(px, py - 4f * scale),
                    size = Size(pr * 1.4f, 8f * scale),
                    cornerRadius = CornerRadius(2f * scale, 2f * scale)
                )
            }

            // Inner core
            drawCircle(
                color = player.avatarColor,
                radius = pr * 0.45f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color.White,
                radius = pr * 0.2f,
                center = Offset(px, py)
            )

            // Player Overhead Name & Health Bars
            val barW = 40f * scale
            val barH = 5f * scale
            val barX = px - barW / 2f
            val barY = py - pr - 18f * scale

            // Name
            val nameResult = textMeasurer.measure(
                text = player.name,
                style = TextStyle(
                    color = if (player.isLocal) NeonCyan else Color.White,
                    fontSize = (9f * scale).coerceAtLeast(8f).sp,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = nameResult,
                topLeft = Offset(px - nameResult.size.width / 2f, barY - 14f * scale)
            )

            // Health bar background
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(barX, barY),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Health fill
            val hpRatio = (player.health / player.maxHealth).coerceIn(0f, 1f)
            drawRoundRect(
                color = if (hpRatio > 0.3f) HealthGreen else NeonRed,
                topLeft = Offset(barX, barY),
                size = Size(barW * hpRatio, barH),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Shield fill bar
            if (player.shield > 0f) {
                val shieldRatio = (player.shield / player.maxShield).coerceIn(0f, 1f)
                drawRoundRect(
                    color = ShieldBlue,
                    topLeft = Offset(barX, barY - 3f * scale),
                    size = Size(barW * shieldRatio, 2.5f * scale),
                    cornerRadius = CornerRadius(1f, 1f)
                )
            }
        }

        // Draw Floating Combat Texts
        for (ft in engine.floatingTexts) {
            val fx = offsetX + ft.x * scale
            val fy = offsetY + ft.y * scale
            val textResult = textMeasurer.measure(
                text = ft.text,
                style = TextStyle(
                    color = ft.color.copy(alpha = ft.alpha),
                    fontSize = (11f * scale).coerceAtLeast(9f).sp,
                    fontWeight = FontWeight.Black
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(fx - textResult.size.width / 2f, fy)
            )
        }

        // Draw Survival Wave Banner
        if (engine.waveTitleAlpha > 0f) {
            val bannerResult = textMeasurer.measure(
                text = engine.waveTitle,
                style = TextStyle(
                    color = NeonYellow.copy(alpha = engine.waveTitleAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            )
            drawText(
                textLayoutResult = bannerResult,
                topLeft = Offset((canvasWidth - bannerResult.size.width) / 2f, 80f)
            )
        }

        // Draw Radar Minimap (Top-right corner)
        drawRadarMinimap(engine, canvasWidth - 110.dp.toPx(), 16.dp.toPx(), 95.dp.toPx(), 70.dp.toPx())
    }
}

private fun DrawScope.drawCyberGrid(x: Float, y: Float, w: Float, h: Float, scale: Float) {
    // Border boundary
    drawRect(
        color = CyberDarkCard.copy(alpha = 0.4f),
        topLeft = Offset(x, y),
        size = Size(w, h)
    )
    drawRect(
        color = CyberDarkBorder,
        topLeft = Offset(x, y),
        size = Size(w, h),
        style = Stroke(width = 3f * scale)
    )

    // Grid lines
    val step = 60f * scale
    var gx = x + step
    while (gx < x + w) {
        drawLine(
            color = Color(0xFF13233C),
            start = Offset(gx, y),
            end = Offset(gx, y + h),
            strokeWidth = 1f
        )
        gx += step
    }

    var gy = y + step
    while (gy < y + h) {
        drawLine(
            color = Color(0xFF13233C),
            start = Offset(x, gy),
            end = Offset(x + w, gy),
            strokeWidth = 1f
        )
        gy += step
    }
}

private fun DrawScope.drawRadarMinimap(engine: GameEngine, rx: Float, ry: Float, rw: Float, rh: Float) {
    // Minimap Background
    drawRoundRect(
        color = CyberDark.copy(alpha = 0.85f),
        topLeft = Offset(rx, ry),
        size = Size(rw, rh),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = NeonCyan.copy(alpha = 0.6f),
        topLeft = Offset(rx, ry),
        size = Size(rw, rh),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )

    val scaleX = rw / engine.worldWidth
    val scaleY = rh / engine.worldHeight

    // Player blips
    for (p in engine.players) {
        if (!p.isAlive) continue
        val bx = rx + p.x * scaleX
        val by = ry + p.y * scaleY
        drawCircle(
            color = if (p.isLocal) NeonCyan else NeonRed,
            radius = if (p.isLocal) 4f else 3f,
            center = Offset(bx, by)
        )
    }

    // Powerup blips
    for (pu in engine.powerUps) {
        val px = rx + pu.x * scaleX
        val py = ry + pu.y * scaleY
        drawCircle(
            color = NeonYellow,
            radius = 2.5f,
            center = Offset(px, py)
        )
    }
}

@Composable
fun KillFeedOverlay(
    killEvents: List<KillEvent>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End
    ) {
        for (event in killEvents.take(4)) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it }
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyberDarkSurface.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (event.isLocalKiller) NeonCyan.copy(alpha = 0.8f) else CyberDarkBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = event.killerName,
                            color = if (event.isLocalKiller) NeonCyan else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = NeonOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = event.victimName,
                            color = if (event.isLocalVictim) NeonRed else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppInviteModal(
    roomCode: String,
    gameMode: GameMode,
    hostName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = WhatsAppGreen
                )
                Text(
                    text = "WhatsApp Challenge Link",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Invite friends on WhatsApp to play live in this room!",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Room Code Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ROOM ACCESS CODE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = roomCode,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = WhatsAppGreen,
                            letterSpacing = 3.sp
                        )
                        Text("Mode: ${gameMode.displayName}", fontSize = 12.sp, color = NeonCyan)
                    }
                }

                // WhatsApp Direct Button
                Button(
                    onClick = {
                        val message = WhatsAppHelper.buildInviteMessage(roomCode, hostName, gameMode)
                        WhatsAppHelper.shareToWhatsApp(context, message)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send to WhatsApp Contact / Group", fontWeight = FontWeight.Bold)
                }

                // Copy Code
                OutlinedButton(
                    onClick = {
                        WhatsAppHelper.copyToClipboard(context, "Room Code", roomCode)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Room Code")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = CyberDarkSurface
    )
}

@Composable
fun QuickEmoteBar(
    onSendEmote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emotes = listOf("🔥", "🎯", "⚡", "👑", "💥", "GG", "Revenge!")
    Row(
        modifier = modifier
            .background(CyberDarkSurface.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
            .border(1.dp, CyberDarkBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (emote in emotes) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSendEmote(emote) }
                    .background(CyberDarkCard)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emote, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
