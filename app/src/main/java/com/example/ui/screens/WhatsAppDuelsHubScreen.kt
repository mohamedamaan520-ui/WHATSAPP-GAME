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
import com.example.data.MatchRecordEntity
import com.example.data.WhatsAppDuelRecordEntity
import com.example.model.GameMode
import com.example.ui.theme.*
import com.example.whatsapp.WhatsAppHelper

@Composable
fun WhatsAppDuelsHubScreen(
    duels: List<WhatsAppDuelRecordEntity>,
    matchRecords: List<MatchRecordEntity>,
    onChallengeBuddy: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var newFriendName by remember { mutableStateOf("") }
    var newFriendPhone by remember { mutableStateOf("") }

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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("WHATSAPP DUELS HUB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    IconButton(
                        onClick = {
                            val generalMsg = WhatsAppHelper.buildInviteMessage(
                                WhatsAppHelper.generateRoomCode(),
                                "CyberChampion",
                                GameMode.ONLINE_WHATSAPP_DUEL
                            )
                            WhatsAppHelper.shareToWhatsApp(context, generalMsg)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = WhatsAppGreen)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Overview Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = WhatsAppGreen)
                            }
                            Column {
                                Text("WhatsApp Duel Records", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Head-to-head 1v1 battle stats with your squad", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val roomCode = WhatsAppHelper.generateRoomCode()
                                val msg = WhatsAppHelper.buildInviteMessage(roomCode, "Me", GameMode.ONLINE_WHATSAPP_DUEL)
                                WhatsAppHelper.shareToWhatsApp(context, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Challenge Any WhatsApp Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Head to Head Buddies List
            item {
                Text(
                    text = "YOUR WHATSAPP RIVALS",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            val buddiesList = duels.ifEmpty {
                WhatsAppHelper.DEFAULT_BUDDIES.map {
                    WhatsAppDuelRecordEntity(it.name, it.wins, it.losses, it.lastPlayedDate)
                }
            }

            items(buddiesList) { buddy ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = buddy.buddyName.take(1),
                                    color = WhatsAppGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(buddy.buddyName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("You ${buddy.wins}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("vs", color = TextMuted, fontSize = 11.sp)
                                    Text("${buddy.losses} Rival", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("• ${buddy.lastPlayedDate}", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }

                        // Instant Rematch Button
                        Button(
                            onClick = {
                                val roomCode = WhatsAppHelper.generateRoomCode()
                                val msg = """
🔫 *CYBERSTRIKE 1v1 REMATCH!* ⚔️
Hey *${buddy.buddyName}*! Ready for a live rematch duel?

🎯 Room Code: *$roomCode*
📲 Join: cyberstrike://join?room=$roomCode
                                """.trimIndent()
                                WhatsAppHelper.shareToWhatsApp(context, msg)
                                onChallengeBuddy(buddy.buddyName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Recent Battle History
            item {
                Text(
                    text = "RECENT MATCH LOGS",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            if (matchRecords.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyberDarkCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No matches recorded yet. Jump into an Online Duel to build your battle history!",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(matchRecords) { rec ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyberDarkCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (rec.isWin) NeonGreen.copy(alpha = 0.5f) else CyberDarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (rec.isWin) "VICTORY" else "DEFEAT",
                                        color = if (rec.isWin) NeonGreen else NeonRed,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                    Text("• ${rec.gameMode.replace("_", " ")}", color = TextSecondary, fontSize = 11.sp)
                                }
                                Text("Opponent: ${rec.opponentOrMap}", color = TextMuted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${rec.kills} Kills", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${rec.score} pts", color = NeonYellow, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
