package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class WeaponId {
    ASSAULT_RIFLE,
    PLASMA_SHOTGUN,
    SNIPER_RAILGUN,
    HOMING_ROCKET,
    TESLA_ARC,
    FLAME_BLASTER
}

data class Weapon(
    val id: WeaponId,
    val name: String,
    val description: String,
    val damage: Float,
    val fireRateMs: Long,
    val bulletSpeed: Float,
    val bulletSpreadRad: Float,
    val pelletCount: Int,
    val bulletRadius: Float,
    val bulletColor: Color,
    val maxAmmo: Int,
    val reloadTimeMs: Long,
    val unlockLevel: Int = 1,
    val unlockCost: Int = 0,
    val isRocket: Boolean = false,
    val isPiercing: Boolean = false,
    val isFlame: Boolean = false
) {
    companion object {
        val ALL = listOf(
            Weapon(
                id = WeaponId.ASSAULT_RIFLE,
                name = "Pulse Carbine",
                description = "Rapid-fire plasma bursts with balanced recoil and high precision.",
                damage = 22f,
                fireRateMs = 130L,
                bulletSpeed = 16f,
                bulletSpreadRad = 0.08f,
                pelletCount = 1,
                bulletRadius = 4f,
                bulletColor = NeonCyan,
                maxAmmo = 30,
                reloadTimeMs = 1200L,
                unlockLevel = 1,
                unlockCost = 0
            ),
            Weapon(
                id = WeaponId.PLASMA_SHOTGUN,
                name = "Scatter Blaster",
                description = "Fires a 5-way spread of devastating close-range kinetic slugs.",
                damage = 18f, // per pellet = 90 total
                fireRateMs = 550L,
                bulletSpeed = 14f,
                bulletSpreadRad = 0.45f,
                pelletCount = 5,
                bulletRadius = 3.5f,
                bulletColor = NeonOrange,
                maxAmmo = 8,
                reloadTimeMs = 1500L,
                unlockLevel = 2,
                unlockCost = 250
            ),
            Weapon(
                id = WeaponId.SNIPER_RAILGUN,
                name = "Hyper Railgun",
                description = "High-voltage piercing beam that penetrates multiple targets.",
                damage = 95f,
                fireRateMs = 900L,
                bulletSpeed = 26f,
                bulletSpreadRad = 0.01f,
                pelletCount = 1,
                bulletRadius = 6f,
                bulletColor = NeonPurple,
                maxAmmo = 4,
                reloadTimeMs = 1800L,
                unlockLevel = 3,
                unlockCost = 500,
                isPiercing = true
            ),
            Weapon(
                id = WeaponId.HOMING_ROCKET,
                name = "Havoc Rocket",
                description = "Explosive projectile dealing heavy area-of-effect splash damage.",
                damage = 80f,
                fireRateMs = 850L,
                bulletSpeed = 11f,
                bulletSpreadRad = 0.05f,
                pelletCount = 1,
                bulletRadius = 7f,
                bulletColor = NeonRed,
                maxAmmo = 5,
                reloadTimeMs = 2000L,
                unlockLevel = 4,
                unlockCost = 800,
                isRocket = true
            ),
            Weapon(
                id = WeaponId.TESLA_ARC,
                name = "Tesla Shock",
                description = "High-frequency electrical bolts that crackle across the battlefield.",
                damage = 28f,
                fireRateMs = 180L,
                bulletSpeed = 18f,
                bulletSpreadRad = 0.15f,
                pelletCount = 1,
                bulletRadius = 5f,
                bulletColor = NeonGreen,
                maxAmmo = 25,
                reloadTimeMs = 1300L,
                unlockLevel = 5,
                unlockCost = 1200
            ),
            Weapon(
                id = WeaponId.FLAME_BLASTER,
                name = "Pyro Core",
                description = "Continuous fiery streams that ignite targets on contact.",
                damage = 14f,
                fireRateMs = 70L,
                bulletSpeed = 10f,
                bulletSpreadRad = 0.35f,
                pelletCount = 2,
                bulletRadius = 6f,
                bulletColor = NeonOrange,
                maxAmmo = 50,
                reloadTimeMs = 1600L,
                unlockLevel = 6,
                unlockCost = 1500,
                isFlame = true
            )
        )

        fun getById(id: WeaponId): Weapon = ALL.find { it.id == id } ?: ALL[0]
    }
}

enum class PowerUpType {
    HEALTH_PACK,
    SHIELD_BOOST,
    QUAD_DAMAGE,
    SPEED_OVERDRIVE,
    NUKE_BOMB
}

data class PowerUp(
    val id: String,
    val type: PowerUpType,
    var x: Float,
    var y: Float,
    val radius: Float = 16f,
    var durationSec: Float = 15f
)

data class Obstacle(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isDestructible: Boolean = false,
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    val color: Color = CyberDarkBorder
)

data class Bullet(
    val id: Long,
    val shooterId: String,
    val shooterName: String,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val damage: Float,
    val radius: Float,
    val color: Color,
    val isRocket: Boolean = false,
    val isPiercing: Boolean = false,
    val isFlame: Boolean = false,
    var lifeSpan: Float = 1.6f,
    val maxLifeSpan: Float = 1.6f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var radius: Float,
    var alpha: Float = 1f,
    var lifeSpan: Float = 0.5f,
    val maxLifeSpan: Float = 0.5f
)

data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1f,
    val vy: Float = -28f,
    var lifeSpan: Float = 0.9f
)

data class Player(
    val id: String,
    var name: String,
    var isLocal: Boolean = false,
    var isBot: Boolean = false,
    var isBotBoss: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var aimAngle: Float = 0f,
    var health: Float = 100f,
    var maxHealth: Float = 100f,
    var shield: Float = 50f,
    var maxShield: Float = 50f,
    var speed: Float = 4.2f,
    var score: Int = 0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var weapon: Weapon = Weapon.ALL[0],
    var currentAmmo: Int = 30,
    var isReloading: Boolean = false,
    var reloadProgress: Float = 0f,
    var avatarColor: Color = NeonCyan,
    var isDashing: Boolean = false,
    var dashCooldownRemaining: Float = 0f,
    var quadDamageTimer: Float = 0f,
    var speedBoostTimer: Float = 0f,
    var lastShotTime: Long = 0L,
    var respawnTimer: Float = 0f,
    var isAlive: Boolean = true,
    var comboCount: Int = 0,
    var comboTimer: Float = 0f,
    var pingMs: Int = 24
)

enum class GameMode(val displayName: String, val description: String) {
    ONLINE_WHATSAPP_DUEL("1v1 WhatsApp Duel", "Live 1v1 battle against your WhatsApp friend"),
    ONLINE_FFA_ARENA("4P Cyber Arena", "Live 4-player deathmatch with real-time scoring"),
    SPLIT_SCREEN_2P("2-Player Same Screen", "Instant 1v1 battle on the same device!"),
    BOT_SURVIVAL("Solo Bot Invasion", "Survive escalating waves of tactical combat drones"),
    SHOOTING_RANGE("Target Range", "Test weapons, DPS, and recoil dynamics")
}

data class LobbyPlayer(
    val id: String,
    val name: String,
    val isHost: Boolean,
    var isReady: Boolean,
    val pingMs: Int,
    val avatarColor: Color,
    val weaponId: WeaponId = WeaponId.ASSAULT_RIFLE,
    var kills: Int = 0,
    var score: Int = 0
)

data class RoomState(
    val roomCode: String,
    val hostName: String,
    val gameMode: GameMode,
    val mapName: String = "Neon Cyber Grid",
    val targetKills: Int = 10,
    val matchTimeSeconds: Int = 180,
    val players: MutableList<LobbyPlayer> = mutableListOf(),
    var isStarted: Boolean = false,
    var isHost: Boolean = false
)

data class KillEvent(
    val id: Long,
    val killerName: String,
    val victimName: String,
    val weaponName: String,
    val isLocalKiller: Boolean = false,
    val isLocalVictim: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: Long,
    val senderName: String,
    val text: String,
    val isWhatsAppInvite: Boolean = false,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class WhatsAppBuddy(
    val id: String,
    val name: String,
    val phoneNumberOrTag: String,
    val wins: Int,
    val losses: Int,
    val lastPlayedDate: String
)
