package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEngine
import com.example.engine.GameEngine
import com.example.model.GameMode
import com.example.ui.components.GameCanvasView
import com.example.ui.components.VirtualJoystick
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplitScreenGameScreen(
    soundEngine: SoundEngine,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var restartTrigger by remember { mutableIntStateOf(0) }

    val engine = remember(restartTrigger) {
        GameEngine(
            gameMode = GameMode.SPLIT_SCREEN_2P,
            localPlayerName = "Player 1 (Blue)",
            remoteOpponentName = "Player 2 (Orange)",
            roomCode = "LOCAL-2P",
            soundEngine = soundEngine,
            targetKills = 5,
            matchDurationSec = 180
        )
    }

    var tickCount by remember { mutableLongStateOf(0L) }
    LaunchedEffect(restartTrigger) {
        var lastTime = System.nanoTime()
        while (!engine.isGameOver) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastTime = now

            engine.update(dt)
            tickCount++
            delay(16L)
        }
    }

    val p1 = engine.localPlayer
    val p2 = engine.player2 ?: engine.players.getOrElse(1) { engine.localPlayer }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
    ) {
        // Center Arena Canvas
        GameCanvasView(
            engine = engine,
            modifier = Modifier.fillMaxSize()
        )

        // TOP CONTROLS FOR PLAYER 2 (Rotated 180 degrees so opposite player can hold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .rotate(180f)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // P2 Move
                VirtualJoystick(
                    joystickSize = 110.dp,
                    knobColor = NeonOrange,
                    label = "P2 MOVE",
                    onValueChange = { vx, vy, _, isEngaged ->
                        // Inverted relative to rotated screen
                        p2.vx = if (isEngaged) -vx else 0f
                        p2.vy = if (isEngaged) -vy else 0f
                    }
                )

                // P2 Vitals
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PLAYER 2: ${p2.kills}/5 KILLS", color = NeonOrange, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = { (p2.health / p2.maxHealth).coerceIn(0f, 1f) },
                        color = NeonOrange,
                        trackColor = CyberDarkCard,
                        modifier = Modifier.width(100.dp).height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                }

                // P2 Aim & Fire
                VirtualJoystick(
                    joystickSize = 110.dp,
                    knobColor = NeonOrange,
                    label = "P2 FIRE",
                    onValueChange = { vx, vy, angle, isEngaged ->
                        if (isEngaged) {
                            val invAngle = (angle + Math.PI.toFloat())
                            p2.aimAngle = invAngle
                            engine.fireWeapon(p2, invAngle)
                        }
                    }
                )
            }
        }

        // CENTER OVERLAY: Exit & Reset buttons
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { restartTrigger++ },
                modifier = Modifier.background(CyberDarkSurface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = NeonCyan)
            }
            IconButton(
                onClick = onExitGame,
                modifier = Modifier.background(CyberDarkSurface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit", tint = NeonRed)
            }
        }

        // BOTTOM CONTROLS FOR PLAYER 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // P1 Move
                VirtualJoystick(
                    joystickSize = 110.dp,
                    knobColor = NeonCyan,
                    label = "P1 MOVE",
                    onValueChange = { vx, vy, _, isEngaged ->
                        p1.vx = if (isEngaged) vx else 0f
                        p1.vy = if (isEngaged) vy else 0f
                    }
                )

                // P1 Vitals
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PLAYER 1: ${p1.kills}/5 KILLS", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = { (p1.health / p1.maxHealth).coerceIn(0f, 1f) },
                        color = NeonCyan,
                        trackColor = CyberDarkCard,
                        modifier = Modifier.width(100.dp).height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                }

                // P1 Aim & Fire
                VirtualJoystick(
                    joystickSize = 110.dp,
                    knobColor = NeonCyan,
                    label = "P1 FIRE",
                    onValueChange = { vx, vy, angle, isEngaged ->
                        if (isEngaged) {
                            p1.aimAngle = angle
                            engine.fireWeapon(p1, angle)
                        }
                    }
                )
            }
        }

        // Game Over Overlay
        if (engine.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberDark.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, NeonYellow),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "👑 ${engine.winnerPlayerName} WINS! 👑",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonYellow
                        )
                        Text(
                            text = "Final: P1 (${p1.kills}) vs P2 (${p2.kills})",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { restartTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                            ) {
                                Text("Rematch", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onExitGame,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("Lobby")
                            }
                        }
                    }
                }
            }
        }
    }
}
