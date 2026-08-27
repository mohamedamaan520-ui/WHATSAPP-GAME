package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.LobbyPlayer
import com.example.model.WeaponId
import com.example.ui.components.WhatsAppInviteModal
import com.example.ui.theme.*
import com.example.whatsapp.WhatsAppHelper

@Composable
fun OnlineMultiplayerLobbyScreen(
    gameMode: GameMode,
    playerName: String,
    onStartMatch: (targetKills: Int, durationSec: Int, roomCode: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roomCode = remember { WhatsAppHelper.generateRoomCode() }
    var showWhatsAppModal by remember { mutableStateOf(false) }

    var targetKills by remember { mutableIntStateOf(10) }
    var matchTimeMinutes by remember { mutableIntStateOf(3) }
    var selectedMap by remember { mutableStateOf("Neon Cyber Grid") }

    // Simulated Lobby Roster
    val players = remember {
        mutableStateListOf(
            LobbyPlayer(
                id = "p1",
                name = "$playerName (Host)",
                isHost = true,
                isReady = true,
                pingMs = 24,
                avatarColor = NeonCyan,
                weaponId = WeaponId.ASSAULT_RIFLE
            ),
            LobbyPlayer(
                id = "p2",
                name = if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL) "WhatsApp Opponent" else "Viper_99",
                isHost = false,
                isReady = true,
                pingMs = 38,
                avatarColor = if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL) WhatsAppGreen else NeonOrange,
                weaponId = WeaponId.PLASMA_SHOTGUN
            )
        )
    }

    LaunchedEffect(Unit) {
        if (gameMode == GameMode.ONLINE_FFA_ARENA) {
            players.add(
                LobbyPlayer(
                    id = "p3",
                    name = "Sarah_Nova",
                    isHost = false,
                    isReady = true,
                    pingMs = 45,
                    avatarColor = NeonPurple,
                    weaponId = WeaponId.SNIPER_RAILGUN
                )
            )
            players.add(
                LobbyPlayer(
                    id = "p4",
                    name = "Ghost_X",
                    isHost = false,
                    isReady = true,
                    pingMs = 29,
                    avatarColor = NeonGreen,
                    weaponId = WeaponId.HOMING_ROCKET
                )
            )
        }
    }

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            Surface(color = CyberDarkSurface, border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL) "1v1 WHATSAPP DUEL LOBBY" else "CYBER ARENA MATCH LOBBY",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showWhatsAppModal = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Invite", tint = WhatsAppGreen)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = CyberDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showWhatsAppModal = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp Invite", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onStartMatch(targetKills, matchTimeMinutes * 60, roomCode)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL) WhatsAppGreen else NeonCyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1.2f).height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START LIVE BATTLE", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Room Access Header Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(color = WhatsAppDark, shape = RoundedCornerShape(4.dp)) {
                                    Text("ROOM CODE", color = WhatsAppGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text("Online WhatsApp Lobby", color = TextSecondary, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonGreen))
                                Text("Live Sync", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = roomCode,
                            color = WhatsAppGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )

                        Text(
                            text = "Share this code or tap invite to launch WhatsApp directly!",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Player Roster
            item {
                Text(
                    text = "COMBATANTS READY (${players.size}/${if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL) 2 else 4})",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            items(players) { p ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(p.avatarColor.copy(alpha = 0.2f))
                                    .border(1.5.dp, p.avatarColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = p.avatarColor, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (p.isHost) {
                                        Surface(color = NeonPurple.copy(alpha = 0.2f), shape = RoundedCornerShape(3.dp)) {
                                            Text("HOST", color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text("Weapon: ${p.weaponId.name.replace("_", " ")}", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Ping
                            Surface(color = CyberDarkSurface, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "${p.pingMs}ms",
                                    color = if (p.pingMs < 50) NeonGreen else NeonOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            // Ready status
                            Surface(
                                color = if (p.isReady) NeonGreen.copy(alpha = 0.2f) else CyberDarkSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (p.isReady) NeonGreen else CyberDarkBorder)
                            ) {
                                Text(
                                    text = if (p.isReady) "READY" else "WAITING",
                                    color = if (p.isReady) NeonGreen else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Match Configuration
            item {
                Text(
                    text = "MATCH RULES",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Kill Limit Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Kill Limit (Score to Win):", color = TextSecondary, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5, 10, 15, 20).forEach { count ->
                                    FilterChip(
                                        selected = targetKills == count,
                                        onClick = { targetKills = count },
                                        label = { Text("$count Kills") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonCyan,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        // Match Duration Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Match Time Limit:", color = TextSecondary, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(2, 3, 5).forEach { mins ->
                                    FilterChip(
                                        selected = matchTimeMinutes == mins,
                                        onClick = { matchTimeMinutes = mins },
                                        label = { Text("$mins Mins") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = WhatsAppGreen,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
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
