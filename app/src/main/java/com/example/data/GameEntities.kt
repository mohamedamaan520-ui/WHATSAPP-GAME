package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.GameMode
import com.example.model.WeaponId

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: String = "local_player",
    val gamerTag: String = "CyberGhost",
    val credits: Int = 350,
    val level: Int = 1,
    val xp: Int = 120,
    val selectedWeaponId: String = WeaponId.ASSAULT_RIFLE.name,
    val selectedSkinHex: Long = 0xFF00F0FF,
    val totalKills: Int = 0,
    val totalDeaths: Int = 0,
    val totalWins: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val gameMode: String,
    val kills: Int,
    val deaths: Int,
    val score: Int,
    val isWin: Boolean,
    val opponentOrMap: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "whatsapp_duel_records")
data class WhatsAppDuelRecordEntity(
    @PrimaryKey val buddyName: String,
    val wins: Int,
    val losses: Int,
    val lastPlayedDate: String
)

@Entity(tableName = "weapon_unlocks")
data class WeaponUnlockEntity(
    @PrimaryKey val weaponId: String,
    val isUnlocked: Boolean = false,
    val upgradeLevel: Int = 1
)
