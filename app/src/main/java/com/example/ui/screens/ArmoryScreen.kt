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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.data.WeaponUnlockEntity
import com.example.model.Weapon
import com.example.model.WeaponId
import com.example.ui.theme.*

@Composable
fun ArmoryScreen(
    profile: PlayerProfileEntity,
    weaponUnlocks: List<WeaponUnlockEntity>,
    onSelectWeapon: (WeaponId) -> Unit,
    onUnlockWeapon: (WeaponId, Int) -> Unit,
    onUpdateGamerTag: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTagDialog by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf(profile.gamerTag) }

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
                        Text("CYBER ARMORY & LOADOUTS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

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
                }
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

            // Profile Customizer Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .border(1.5.dp, NeonCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan)
                            }
                            Column {
                                Text("Gamer Nickname", color = TextMuted, fontSize = 11.sp)
                                Text(profile.gamerTag, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { showTagDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Edit Tag", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Weapons List Header
            item {
                Text(
                    text = "ARSENAL UPGRADES",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            items(Weapon.ALL) { weapon ->
                val isUnlocked = weapon.unlockCost == 0 || weaponUnlocks.any { it.weaponId == weapon.id.name }
                val isEquipped = profile.selectedWeaponId == weapon.id.name

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isEquipped) weapon.bulletColor else CyberDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(weapon.bulletColor.copy(alpha = 0.2f))
                                        .border(1.dp, weapon.bulletColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = weapon.bulletColor)
                                }
                                Column {
                                    Text(weapon.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(weapon.description, color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            if (isEquipped) {
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                                ) {
                                    Text("EQUIPPED", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        // Stat Bars
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatRow("Damage", (weapon.damage * weapon.pelletCount) / 120f, weapon.bulletColor)
                            StatRow("Fire Rate", (1200f - weapon.fireRateMs) / 1150f, weapon.bulletColor)
                            StatRow("Velocity", weapon.bulletSpeed / 30f, weapon.bulletColor)
                            StatRow("Magazine", weapon.maxAmmo / 50f, weapon.bulletColor)
                        }

                        // Action Button
                        if (isUnlocked) {
                            if (!isEquipped) {
                                Button(
                                    onClick = { onSelectWeapon(weapon.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = weapon.bulletColor, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text("Equip Weapon", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onUnlockWeapon(weapon.id, weapon.unlockCost) },
                                enabled = profile.credits >= weapon.unlockCost,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonYellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unlock for ${weapon.unlockCost} Credits", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Change Gamer Nickname", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberDarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tagInput.isNotBlank()) {
                            onUpdateGamerTag(tagInput.trim())
                            showTagDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CyberDarkSurface
        )
    }
}

@Composable
fun StatRow(label: String, ratio: Float, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(70.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0.05f, 1f) },
            color = color,
            trackColor = CyberDarkCard,
            modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(2.dp))
        )
    }
}
