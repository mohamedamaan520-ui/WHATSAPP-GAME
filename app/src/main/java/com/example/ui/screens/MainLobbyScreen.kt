package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerProfileEntity
import com.example.model.GameMode
import com.example.ui.theme.*
import com.example.whatsapp.WhatsAppHelper

@Composable
fun MainLobbyScreen(
    profile: PlayerProfileEntity,
    onStartGame: (GameMode, String, String) -> Unit,
    onOpenOnlineLobby: (GameMode) -> Unit,
    onOpenArmory: () -> Unit,
    onOpenWhatsAppHub: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            Surface(
                color = CyberDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.5.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.gamerTag,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NeonPurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple)
                                ) {
                                    Text(
                                        text = "LVL ${profile.level}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonPurple,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Wins: ${profile.totalWins}  •  Kills: ${profile.totalKills}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Credits & Sound Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = CyberDarkCard,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(16.dp))
                                Text("${profile.credits}", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        IconButton(
                            onClick = onToggleSound,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (profile.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Toggle Sound",
                                tint = if (profile.soundEnabled) NeonCyan else TextMuted
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already here */ },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Play") },
                    label = { Text("Battle") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = CyberDarkCard
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenArmory,
                    icon = { Icon(Icons.Default.Build, contentDescription = "Armory") },
                    label = { Text("Armory") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenWhatsAppHub,
                    icon = { Icon(Icons.Default.Chat, contentDescription = "WhatsApp Hub") },
                    label = { Text("WhatsApp Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = WhatsAppGreen,
                        unselectedTextColor = WhatsAppGreen
                    )
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hero Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(175.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "Hero banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Shade
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, CyberDark.copy(alpha = 0.85f), CyberDark)
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Surface(
                                color = WhatsAppGreen,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text("LIVE MULTIPLAYER & WHATSAPP DUELS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CYBERSTRIKE ARENA",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Dual-stick tactical top-down shooter with real-time PvP",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // WhatsApp Quick Matchmaking Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = WhatsAppGreen)
                                Text("WhatsApp Live Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Surface(
                                color = WhatsAppDark,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("1-Click Connect", color = WhatsAppGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }

                        Text(
                            text = "Create a private duel room, share the invite directly to WhatsApp, and start fighting in real-time!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onOpenOnlineLobby(GameMode.ONLINE_WHATSAPP_DUEL) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Host 1v1 Room", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showJoinDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Join Code", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Game Modes Header
            item {
                Text(
                    text = "SELECT COMBAT MODE",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Mode 1: 4-Player Free-for-All Arena
            item {
                ModeCard(
                    title = "4-Player Cyber Arena (FFA)",
                    subtitle = "Real-time 4-player free-for-all deathmatch with live leaderboard",
                    icon = Icons.Default.Groups,
                    badgeText = "ONLINE ARENA",
                    badgeColor = NeonPurple,
                    accentColor = NeonPurple,
                    onClick = { onOpenOnlineLobby(GameMode.ONLINE_FFA_ARENA) }
                )
            }

            // Mode 2: 2-Player Same-Screen Pass & Play Duel
            item {
                ModeCard(
                    title = "2-Player Same Screen Duel",
                    subtitle = "Instant local 1v1 battle on this device (Top vs Bottom controllers)",
                    icon = Icons.Default.TouchApp,
                    badgeText = "LOCAL 2P DUEL",
                    badgeColor = NeonOrange,
                    accentColor = NeonOrange,
                    onClick = { onStartGame(GameMode.SPLIT_SCREEN_2P, "Player 1", "Player 2") }
                )
            }

            // Mode 3: Solo Bot Invasion Survival
            item {
                ModeCard(
                    title = "Solo Bot Invasion",
                    subtitle = "Survive escalating waves of tactical combat drones and heavy mech bosses",
                    icon = Icons.Default.Security,
                    badgeText = "WAVE SURVIVAL",
                    badgeColor = NeonGreen,
                    accentColor = NeonGreen,
                    onClick = { onStartGame(GameMode.BOT_SURVIVAL, profile.gamerTag, "TITAN-X") }
                )
            }

            // Mode 4: Target Shooting Range
            item {
                ModeCard(
                    title = "Target Shooting Range",
                    subtitle = "Test pulse carbines, railguns, and rocket launchers with live DPS counters",
                    icon = Icons.Default.AdsClick,
                    badgeText = "PRACTICE",
                    badgeColor = TextSecondary,
                    accentColor = NeonCyan,
                    onClick = { onStartGame(GameMode.SHOOTING_RANGE, profile.gamerTag, "Target Dummy") }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Join with Code Dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text("Join Online Room", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the 6-character room code from your WhatsApp challenge invite or paste the link:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = {
                            joinCodeInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("e.g. CYB-884", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            unfocusedBorderColor = CyberDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = NeonRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = WhatsAppHelper.extractRoomCodeFromUriOrText(joinCodeInput)
                        if (parsed != null) {
                            showJoinDialog = false
                            onStartGame(GameMode.ONLINE_WHATSAPP_DUEL, profile.gamerTag, "WhatsApp Host")
                        } else {
                            errorMessage = "Invalid code. Please enter a valid 6-char code (e.g. ABC-123)"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Text("Join & Play", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CyberDarkSurface
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    badgeColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, badgeColor)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
