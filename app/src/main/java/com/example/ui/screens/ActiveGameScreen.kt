package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEngine
import com.example.engine.GameEngine
import com.example.model.*
import com.example.ui.components.GameCanvasView
import com.example.ui.components.KillFeedOverlay
import com.example.ui.components.QuickEmoteBar
import com.example.ui.components.VirtualJoystick
import com.example.ui.components.WhatsAppInviteModal
import com.example.ui.theme.*
import com.example.whatsapp.WhatsAppHelper
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun ActiveGameScreen(
    gameMode: GameMode,
    playerName: String,
    opponentName: String,
    roomCode: String,
    targetKills: Int,
    matchDurationSec: Int,
    soundEngine: SoundEngine,
    onGameOver: (kills: Int, score: Int, isWin: Boolean, opponent: String) -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val engine = remember {
        GameEngine(
            gameMode = gameMode,
            localPlayerName = playerName,
            remoteOpponentName = opponentName,
            roomCode = roomCode,
            soundEngine = soundEngine,
            targetKills = targetKills,
            matchDurationSec = matchDurationSec
        )
    }

    var isPaused by remember { mutableStateOf(false) }
    var showWhatsAppModal by remember { mutableStateOf(false) }
    var matchRecorded by remember { mutableStateOf(false) }

    // High-FPS 60 FPS Game Loop
    var tickCount by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isPaused) {
        var lastTime = System.nanoTime()
        while (!isPaused) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastTime = now

            engine.update(dt)
            tickCount++

            if (engine.isGameOver && !matchRecorded) {
                matchRecorded = true
                val lp = engine.localPlayer
                onGameOver(lp.kills, lp.score, engine.isVictory, opponentName)
            }

            delay(16L) // ~60 FPS
        }
    }

    val localPlayer = engine.localPlayer

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
    ) {
        // 1. Core Arena Render View
        GameCanvasView(
            engine = engine,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Kill Feed Overlay (Top-Right under HUD)
        KillFeedOverlay(
            killEvents = engine.killEvents,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp)
        )

        // 3. Top Combat HUD Header
        Surface(
            color = CyberDarkSurface.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Match Score / Kills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = CyberDarkCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.SportsScore, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${localPlayer.kills} / $targetKills KILLS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (gameMode == GameMode.BOT_SURVIVAL) {
                        Surface(
                            color = NeonOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange)
                        ) {
                            Text(
                                text = "WAVE ${engine.currentWave}",
                                color = NeonOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Center: Match Timer & Ping
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = (engine.matchTimeRemaining / 60).toInt()
                    val seconds = (engine.matchTimeRemaining % 60).toInt()
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = if (engine.matchTimeRemaining < 30f) NeonRed else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonGreen))
                        Text(
                            text = "${localPlayer.pingMs}ms  •  $roomCode",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                // Right: Pause & WhatsApp Share Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { showWhatsAppModal = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = WhatsAppGreen)
                    }

                    IconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 4. Bottom Controls Layer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            // Quick WhatsApp Emote Bar
            QuickEmoteBar(
                onSendEmote = { emote ->
                    engine.sendQuickChatMessage(localPlayer.name, emote)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // LEFT: Movement Virtual Joystick
                VirtualJoystick(
                    joystickSize = 125.dp,
                    knobColor = NeonCyan,
                    label = "MOVE",
                    onValueChange = { vx, vy, _, isEngaged ->
                        localPlayer.vx = if (isEngaged) vx else 0f
                        localPlayer.vy = if (isEngaged) vy else 0f
                    }
                )

                // CENTER: Vitals HUD & Weapon Select Carousel & Dash
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Health & Shield Bars
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.width(130.dp)
                    ) {
                        // Shield Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SHIELD", fontSize = 9.sp, color = ShieldBlue, fontWeight = FontWeight.Bold)
                            Text("${localPlayer.shield.toInt()}", fontSize = 9.sp, color = ShieldBlue, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { (localPlayer.shield / localPlayer.maxShield).coerceIn(0f, 1f) },
                            color = ShieldBlue,
                            trackColor = CyberDarkCard,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )

                        // Health Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("HP", fontSize = 9.sp, color = HealthGreen, fontWeight = FontWeight.Bold)
                            Text("${localPlayer.health.toInt()}", fontSize = 9.sp, color = HealthGreen, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { (localPlayer.health / localPlayer.maxHealth).coerceIn(0f, 1f) },
                            color = if (localPlayer.health > 30f) HealthGreen else NeonRed,
                            trackColor = CyberDarkCard,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                    }

                    // Dash & Reload Action Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Dash Button
                        Button(
                            onClick = { engine.triggerDash(localPlayer) },
                            enabled = localPlayer.dashCooldownRemaining <= 0f,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (localPlayer.dashCooldownRemaining <= 0f) NeonCyan else CyberDarkCard,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.size(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (localPlayer.dashCooldownRemaining > 0f) {
                                Text("${localPlayer.dashCooldownRemaining.toInt()}s", fontSize = 11.sp, color = TextMuted)
                            } else {
                                Icon(Icons.Default.ElectricBolt, contentDescription = "Dash", modifier = Modifier.size(22.dp))
                            }
                        }

                        // Reload Button with Ammo Counter
                        Button(
                            onClick = { engine.reloadPlayer(localPlayer) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberDarkCard,
                                contentColor = NeonOrange
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.6f)),
                            modifier = Modifier.height(44.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (localPlayer.isReloading) "..." else "${localPlayer.currentAmmo}/${localPlayer.weapon.maxAmmo}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Weapon Quick Switch Carousel
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.widthIn(max = 160.dp)
                    ) {
                        items(Weapon.ALL) { w ->
                            val isSelected = localPlayer.weapon.id == w.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) w.bulletColor.copy(alpha = 0.25f) else CyberDarkCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) w.bulletColor else CyberDarkBorder
                                ),
                                modifier = Modifier.clickable {
                                    localPlayer.weapon = w
                                    localPlayer.currentAmmo = w.maxAmmo
                                    localPlayer.isReloading = false
                                }
                            ) {
                                Text(
                                    text = w.name.take(6),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) w.bulletColor else TextMuted,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // RIGHT: Aim & Fire Virtual Joystick
                VirtualJoystick(
                    joystickSize = 125.dp,
                    knobColor = NeonOrange,
                    label = "AIM & FIRE",
                    onValueChange = { vx, vy, angle, isEngaged ->
                        if (isEngaged) {
                            localPlayer.aimAngle = angle
                            engine.fireWeapon(localPlayer, angle)
                        }
                    }
                )
            }
        }

        // 5. Game Over / Match Results Overlay Modal
        if (engine.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberDark.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (engine.isVictory) NeonGreen else NeonRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title
                        Text(
                            text = if (engine.isVictory) "🏆 VICTORY! 👑" else "💥 MATCH OVER ⚔️",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (engine.isVictory) NeonGreen else NeonRed
                        )

                        Text(
                            text = "Winner: ${engine.winnerPlayerName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        // Stats Summary Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberDarkCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("KILLS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("${localPlayer.kills}", fontSize = 18.sp, color = NeonCyan, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("DEATHS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("${localPlayer.deaths}", fontSize = 18.sp, color = NeonRed, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SCORE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("${localPlayer.score}", fontSize = 18.sp, color = NeonYellow, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        // WhatsApp Share Result Card Button
                        Button(
                            onClick = {
                                val bragMessage = WhatsAppHelper.buildScoreChallengeMessage(
                                    playerName = localPlayer.name,
                                    kills = localPlayer.kills,
                                    score = localPlayer.score,
                                    gameMode = gameMode,
                                    botWaves = engine.currentWave
                                )
                                WhatsAppHelper.shareToWhatsApp(context, bragMessage)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Brag / Share on WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        // Exit to Lobby
                        OutlinedButton(
                            onClick = onExitGame,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Lobby")
                        }
                    }
                }
            }
        }

        // 6. Pause Modal
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberDark.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberDarkBorder),
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("GAME PAUSED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)

                        Button(
                            onClick = { isPaused = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Resume Game", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onExitGame,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Leave Match")
                        }
                    }
                }
            }
        }
    }

    if (showWhatsAppModal) {
        WhatsAppInviteModal(
            roomCode = roomCode,
            gameMode = gameMode,
            hostName = playerName,
            onDismiss = { showWhatsAppModal = false }
        )
    }
}
