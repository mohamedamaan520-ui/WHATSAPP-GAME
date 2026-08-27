package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEngine
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    val gameMode: GameMode,
    val localPlayerName: String = "CyberGhost",
    val remoteOpponentName: String = "WhatsApp Opponent",
    val roomCode: String = "CYBER-101",
    val soundEngine: SoundEngine,
    val targetKills: Int = 10,
    val matchDurationSec: Int = 180
) {
    val worldWidth = 1200f
    val worldHeight = 900f

    val players = mutableListOf<Player>()
    val bullets = mutableListOf<Bullet>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()
    val powerUps = mutableListOf<PowerUp>()
    val obstacles = mutableListOf<Obstacle>()
    val killEvents = mutableListOf<KillEvent>()
    val chatMessages = mutableListOf<ChatMessage>()

    var matchTimeRemaining = matchDurationSec.toFloat()
    var isGameOver = false
    var winnerPlayerName: String? = null
    var isVictory = false

    // Survival mode waves
    var currentWave = 1
    var waveEnemiesRemaining = 0
    var waveSpawnTimer = 0f
    var waveTitle = ""
    var waveTitleAlpha = 0f

    private var bulletIdCounter = 1L
    private var textIdCounter = 1L
    private var killIdCounter = 1L
    private var chatIdCounter = 1L

    // Local player reference
    val localPlayer: Player
        get() = players.firstOrNull { it.isLocal } ?: players[0]

    // Player 2 reference for Split-Screen mode
    val player2: Player?
        get() = if (gameMode == GameMode.SPLIT_SCREEN_2P) players.getOrNull(1) else null

    init {
        setupArena()
        setupPlayers()
        if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL || gameMode == GameMode.ONLINE_FFA_ARENA) {
            chatMessages.add(
                ChatMessage(
                    chatIdCounter++,
                    "SYSTEM",
                    "🎮 WhatsApp Room [$roomCode] Connected! Live PvP Started.",
                    isSystem = true
                )
            )
        }
    }

    private fun setupArena() {
        obstacles.clear()
        powerUps.clear()

        // Border walls
        obstacles.add(Obstacle("w_top", 0f, 0f, worldWidth, 20f, false))
        obstacles.add(Obstacle("w_bottom", 0f, worldHeight - 20f, worldWidth, 20f, false))
        obstacles.add(Obstacle("w_left", 0f, 0f, 20f, worldHeight, false))
        obstacles.add(Obstacle("w_right", worldWidth - 20f, 0f, 20f, worldHeight, false))

        // Center Obstacles & Neon Bunkers
        obstacles.add(Obstacle("b_center_l", 360f, 260f, 40f, 380f, false, color = NeonCyan))
        obstacles.add(Obstacle("b_center_r", 800f, 260f, 40f, 380f, false, color = NeonOrange))
        obstacles.add(Obstacle("b_mid_top", 500f, 160f, 200f, 35f, false, color = NeonPurple))
        obstacles.add(Obstacle("b_mid_bot", 500f, 705f, 200f, 35f, false, color = NeonPurple))

        // Destructible Energy Crates
        obstacles.add(Obstacle("crate_1", 220f, 200f, 45f, 45f, true, 80f, 80f, NeonYellow))
        obstacles.add(Obstacle("crate_2", 220f, 655f, 45f, 45f, true, 80f, 80f, NeonYellow))
        obstacles.add(Obstacle("crate_3", 935f, 200f, 45f, 45f, true, 80f, 80f, NeonYellow))
        obstacles.add(Obstacle("crate_4", 935f, 655f, 45f, 45f, true, 80f, 80f, NeonYellow))
        obstacles.add(Obstacle("crate_c1", 575f, 425f, 50f, 50f, true, 120f, 120f, WhatsAppGreen))

        // Initial Power-ups
        spawnPowerUp(PowerUpType.QUAD_DAMAGE, 600f, 280f)
        spawnPowerUp(PowerUpType.HEALTH_PACK, 160f, 450f)
        spawnPowerUp(PowerUpType.SHIELD_BOOST, 1040f, 450f)
        spawnPowerUp(PowerUpType.SPEED_OVERDRIVE, 600f, 620f)
    }

    private fun setupPlayers() {
        players.clear()
        when (gameMode) {
            GameMode.ONLINE_WHATSAPP_DUEL -> {
                // Local Player (Host/Client)
                players.add(
                    Player(
                        id = "p_local",
                        name = localPlayerName,
                        isLocal = true,
                        x = 180f,
                        y = 450f,
                        avatarColor = NeonCyan,
                        weapon = Weapon.ALL[0]
                    )
                )
                // WhatsApp Opponent
                players.add(
                    Player(
                        id = "p_remote_1",
                        name = remoteOpponentName,
                        isLocal = false,
                        isBot = true, // Simulated network peer with high fidelity
                        x = 1020f,
                        y = 450f,
                        avatarColor = WhatsAppGreen,
                        weapon = Weapon.ALL[1],
                        pingMs = Random.nextInt(28, 48)
                    )
                )
            }
            GameMode.ONLINE_FFA_ARENA -> {
                players.add(
                    Player(
                        id = "p_local",
                        name = localPlayerName,
                        isLocal = true,
                        x = 200f,
                        y = 250f,
                        avatarColor = NeonCyan,
                        weapon = Weapon.ALL[0]
                    )
                )
                players.add(
                    Player(
                        id = "p_remote_1",
                        name = "Viper (WhatsApp)",
                        isLocal = false,
                        isBot = true,
                        x = 1000f,
                        y = 250f,
                        avatarColor = NeonOrange,
                        weapon = Weapon.ALL[1],
                        pingMs = 32
                    )
                )
                players.add(
                    Player(
                        id = "p_remote_2",
                        name = "Alex_H",
                        isLocal = false,
                        isBot = true,
                        x = 200f,
                        y = 650f,
                        avatarColor = WhatsAppGreen,
                        weapon = Weapon.ALL[2],
                        pingMs = 45
                    )
                )
                players.add(
                    Player(
                        id = "p_remote_3",
                        name = "Sarah_Nova",
                        isLocal = false,
                        isBot = true,
                        x = 1000f,
                        y = 650f,
                        avatarColor = NeonPurple,
                        weapon = Weapon.ALL[3],
                        pingMs = 28
                    )
                )
            }
            GameMode.SPLIT_SCREEN_2P -> {
                // Player 1 (Bottom half controls)
                players.add(
                    Player(
                        id = "p1",
                        name = "Player 1 (Blue)",
                        isLocal = true,
                        x = 300f,
                        y = 450f,
                        avatarColor = NeonCyan,
                        weapon = Weapon.ALL[0]
                    )
                )
                // Player 2 (Top half controls)
                players.add(
                    Player(
                        id = "p2",
                        name = "Player 2 (Orange)",
                        isLocal = false, // Controlled by Player 2 touch controls
                        isBot = false,
                        x = 900f,
                        y = 450f,
                        avatarColor = NeonOrange,
                        weapon = Weapon.ALL[1]
                    )
                )
            }
            GameMode.BOT_SURVIVAL -> {
                players.add(
                    Player(
                        id = "p_local",
                        name = localPlayerName,
                        isLocal = true,
                        x = 600f,
                        y = 450f,
                        avatarColor = NeonCyan,
                        weapon = Weapon.ALL[0]
                    )
                )
                startWave(1)
            }
            GameMode.SHOOTING_RANGE -> {
                players.add(
                    Player(
                        id = "p_local",
                        name = localPlayerName,
                        isLocal = true,
                        x = 250f,
                        y = 450f,
                        avatarColor = NeonCyan,
                        weapon = Weapon.ALL[0]
                    )
                )
                // Practice Target Dummies
                for (i in 0 until 4) {
                    val ty = 200f + i * 160f
                    players.add(
                        Player(
                            id = "dummy_$i",
                            name = "Target Dummy #${i + 1}",
                            isLocal = false,
                            isBot = false,
                            x = 950f,
                            y = ty,
                            health = 200f,
                            maxHealth = 200f,
                            avatarColor = NeonRed,
                            speed = 0f
                        )
                    )
                }
            }
        }
    }

    private fun startWave(wave: Int) {
        currentWave = wave
        waveEnemiesRemaining = 3 + wave * 2
        waveTitle = if (wave % 5 == 0) "⚠️ BOSS WAVE $wave: TITAN MECH CYBORG! ⚠️" else "⚡ WAVE $wave: CYBER DRONE INVASION ⚡"
        waveTitleAlpha = 1f
        soundEngine.playPowerUp()

        // Remove dead bots
        players.removeAll { !it.isLocal }

        val isBossWave = (wave % 5 == 0)
        val count = if (isBossWave) 1 else (3 + wave)
        for (i in 0 until count) {
            val spawnAngle = (i.toFloat() / count) * 2f * PI.toFloat()
            val sx = (600f + cos(spawnAngle) * 450f).coerceIn(80f, worldWidth - 80f)
            val sy = (450f + sin(spawnAngle) * 350f).coerceIn(80f, worldHeight - 80f)

            val botWeapon = when {
                isBossWave -> Weapon.ALL[3] // Rockets
                wave >= 4 -> Weapon.ALL[Random.nextInt(Weapon.ALL.size)]
                wave >= 2 -> Weapon.ALL[Random.nextInt(3)]
                else -> Weapon.ALL[0]
            }

            val maxHp = if (isBossWave) 500f + wave * 100f else 60f + wave * 15f
            players.add(
                Player(
                    id = "bot_w${wave}_$i",
                    name = if (isBossWave) "TITAN-MECH #$wave" else "Drone-${Random.nextInt(10, 99)}",
                    isLocal = false,
                    isBot = true,
                    isBotBoss = isBossWave,
                    x = sx,
                    y = sy,
                    health = maxHp,
                    maxHealth = maxHp,
                    shield = if (isBossWave) 200f else 20f,
                    maxShield = if (isBossWave) 200f else 20f,
                    speed = if (isBossWave) 2.2f else 3.2f + min(wave * 0.2f, 2f),
                    avatarColor = if (isBossWave) NeonRed else Color(0xFFFF5555),
                    weapon = botWeapon
                )
            )
        }
    }

    fun spawnPowerUp(type: PowerUpType, x: Float, y: Float) {
        powerUps.removeAll { hypot(it.x - x, it.y - y) < 40f }
        powerUps.add(PowerUp("pu_${System.nanoTime()}", type, x, y))
    }

    // MAIN GAME UPDATE TICK (Called 60 FPS)
    fun update(dt: Float) {
        if (isGameOver) return

        // 1. Update Match Timer
        if (gameMode != GameMode.SHOOTING_RANGE) {
            matchTimeRemaining -= dt
            if (matchTimeRemaining <= 0f) {
                matchTimeRemaining = 0f
                endMatchByTime()
                return
            }
        }

        // Fade wave banner
        if (waveTitleAlpha > 0f) {
            waveTitleAlpha = (waveTitleAlpha - dt * 0.4f).coerceAtLeast(0f)
        }

        // 2. Update Players
        val playersCopy = players.toList()
        for (player in playersCopy) {
            updatePlayer(player, dt)
        }

        // 3. Update Bullets & Collisions
        updateBullets(dt)

        // 4. Update Particles & Floating Text
        updateFX(dt)

        // 5. Check Wave Progression in Survival Mode
        if (gameMode == GameMode.BOT_SURVIVAL) {
            val aliveBots = players.count { !it.isLocal && it.isAlive }
            if (aliveBots == 0 && waveTitleAlpha <= 0.2f) {
                startWave(currentWave + 1)
            }
        }

        // 6. Spawn Random PowerUps periodically
        if (Random.nextFloat() < 0.003f && powerUps.size < 4) {
            val rx = Random.nextFloat() * (worldWidth - 200f) + 100f
            val ry = Random.nextFloat() * (worldHeight - 200f) + 100f
            val types = PowerUpType.values()
            spawnPowerUp(types.random(), rx, ry)
        }
    }

    private fun updatePlayer(player: Player, dt: Float) {
        // Handle Respawn
        if (!player.isAlive) {
            player.respawnTimer -= dt
            if (player.respawnTimer <= 0f) {
                respawnPlayer(player)
            }
            return
        }

        // Dash cooldown
        if (player.dashCooldownRemaining > 0f) {
            player.dashCooldownRemaining -= dt
        }

        // Power-up buffs
        if (player.quadDamageTimer > 0f) player.quadDamageTimer -= dt
        if (player.speedBoostTimer > 0f) player.speedBoostTimer -= dt

        // Combo timeout
        if (player.comboTimer > 0f) {
            player.comboTimer -= dt
            if (player.comboTimer <= 0f) {
                player.comboCount = 0
            }
        }

        // Reload
        if (player.isReloading) {
            player.reloadProgress += dt / (player.weapon.reloadTimeMs / 1000f)
            if (player.reloadProgress >= 1f) {
                player.isReloading = false
                player.reloadProgress = 0f
                player.currentAmmo = player.weapon.maxAmmo
            }
        }

        // AI Bot Logic
        if (player.isBot) {
            updateBotAI(player, dt)
        }

        // Velocity & Movement
        val actualSpeed = player.speed * (if (player.speedBoostTimer > 0f) 1.5f else 1f) * (if (player.isDashing) 2.5f else 1f)
        val nextX = player.x + player.vx * actualSpeed
        val nextY = player.y + player.vy * actualSpeed

        // Collision with arena borders
        val pRadius = if (player.isBotBoss) 30f else 18f
        val clampedX = nextX.coerceIn(pRadius + 20f, worldWidth - pRadius - 20f)
        val clampedY = nextY.coerceIn(pRadius + 20f, worldHeight - pRadius - 20f)

        // Collision with obstacles
        var canMoveX = true
        var canMoveY = true
        for (obs in obstacles) {
            if (circleIntersectsRect(clampedX, player.y, pRadius, obs.x, obs.y, obs.width, obs.height)) {
                canMoveX = false
            }
            if (circleIntersectsRect(player.x, clampedY, pRadius, obs.x, obs.y, obs.width, obs.height)) {
                canMoveY = false
            }
        }

        if (canMoveX) player.x = clampedX
        if (canMoveY) player.y = clampedY

        // Power-up pickups
        val puIterator = powerUps.iterator()
        while (puIterator.hasNext()) {
            val pu = puIterator.next()
            if (hypot(player.x - pu.x, player.y - pu.y) < pRadius + pu.radius) {
                applyPowerUp(player, pu.type)
                puIterator.remove()
                soundEngine.playPowerUp()
                createPowerUpBurst(pu.x, pu.y, pu.type)
            }
        }
    }

    private fun applyPowerUp(player: Player, type: PowerUpType) {
        when (type) {
            PowerUpType.HEALTH_PACK -> {
                val healed = min(50f, player.maxHealth - player.health)
                player.health = min(player.maxHealth, player.health + 50f)
                addFloatingText("+${healed.toInt()} HP", player.x, player.y - 20f, HealthGreen)
            }
            PowerUpType.SHIELD_BOOST -> {
                player.shield = player.maxShield
                addFloatingText("+SHIELD FULL", player.x, player.y - 20f, ShieldBlue)
            }
            PowerUpType.QUAD_DAMAGE -> {
                player.quadDamageTimer = 10f
                addFloatingText("QUAD DAMAGE!", player.x, player.y - 20f, NeonOrange)
            }
            PowerUpType.SPEED_OVERDRIVE -> {
                player.speedBoostTimer = 8f
                addFloatingText("HYPER SPEED!", player.x, player.y - 20f, NeonGreen)
            }
            PowerUpType.NUKE_BOMB -> {
                addFloatingText("NUKE BLAST!", player.x, player.y - 20f, NeonRed)
                soundEngine.playExplosion()
                // Damage all other enemies
                for (other in players) {
                    if (other.id != player.id && other.isAlive) {
                        damagePlayer(other, 75f, player, false)
                    }
                }
            }
        }
    }

    private fun updateBotAI(bot: Player, dt: Float) {
        // Target nearest opponent
        val target = players.filter { it.id != bot.id && it.isAlive }
            .minByOrNull { hypot(it.x - bot.x, it.y - bot.y) } ?: return

        val dist = hypot(target.x - bot.x, target.y - bot.y)
        val angleToTarget = atan2(target.y - bot.y, target.x - bot.x)
        bot.aimAngle = angleToTarget

        // Tactical Movement: Strafe, retreat when low HP, seek powerups
        val lowHp = (bot.health < 40f)
        val nearbyPowerUp = if (lowHp) powerUps.minByOrNull { hypot(it.x - bot.x, it.y - bot.y) } else null

        if (nearbyPowerUp != null && hypot(nearbyPowerUp.x - bot.x, nearbyPowerUp.y - bot.y) < 300f) {
            val puAngle = atan2(nearbyPowerUp.y - bot.y, nearbyPowerUp.x - bot.x)
            bot.vx = cos(puAngle)
            bot.vy = sin(puAngle)
        } else if (dist > 280f) {
            // Move closer
            bot.vx = cos(angleToTarget)
            bot.vy = sin(angleToTarget)
        } else if (dist < 120f) {
            // Back up
            bot.vx = -cos(angleToTarget)
            bot.vy = -sin(angleToTarget)
        } else {
            // Strafe sideways
            val strafeAngle = angleToTarget + (if (Random.nextBoolean()) 1.5f else -1.5f)
            bot.vx = cos(strafeAngle) * 0.7f
            bot.vy = sin(strafeAngle) * 0.7f
        }

        // Fire at target if line of sight is clear
        val now = System.currentTimeMillis()
        if (now - bot.lastShotTime >= bot.weapon.fireRateMs && dist < 500f) {
            fireWeapon(bot, bot.aimAngle + (Random.nextFloat() - 0.5f) * 0.15f)
        }

        // Rare WhatsApp quick emote banter from simulated players
        if (Random.nextFloat() < 0.0008f && (gameMode == GameMode.ONLINE_WHATSAPP_DUEL || gameMode == GameMode.ONLINE_FFA_ARENA)) {
            val emotes = listOf("Nice shot! 🔥", "Watch your back! 🎯", "Revenge incoming! ⚔️", "WhatsApp duel is on! 💥", "GG! 👑")
            chatMessages.add(ChatMessage(chatIdCounter++, bot.name, emotes.random()))
        }
    }

    fun fireWeapon(player: Player, angle: Float) {
        if (!player.isAlive) return
        val now = System.currentTimeMillis()
        if (now - player.lastShotTime < player.weapon.fireRateMs) return
        if (player.isReloading) return

        if (player.currentAmmo <= 0) {
            reloadPlayer(player)
            return
        }

        player.lastShotTime = now
        player.currentAmmo--

        val weapon = player.weapon
        val isQuad = player.quadDamageTimer > 0f
        val damageMultiplier = if (isQuad) 2f else 1f

        val spreadStep = if (weapon.pelletCount > 1) weapon.bulletSpreadRad / (weapon.pelletCount - 1) else 0f
        val startSpread = if (weapon.pelletCount > 1) -weapon.bulletSpreadRad / 2f else 0f

        val muzzleDist = if (player.isBotBoss) 36f else 24f
        val originX = player.x + cos(angle) * muzzleDist
        val originY = player.y + sin(angle) * muzzleDist

        for (i in 0 until weapon.pelletCount) {
            val shotAngle = angle + startSpread + i * spreadStep + (Random.nextFloat() - 0.5f) * 0.04f
            val bvx = cos(shotAngle) * weapon.bulletSpeed
            val bvy = sin(shotAngle) * weapon.bulletSpeed

            bullets.add(
                Bullet(
                    id = bulletIdCounter++,
                    shooterId = player.id,
                    shooterName = player.name,
                    x = originX,
                    y = originY,
                    vx = bvx,
                    vy = bvy,
                    damage = weapon.damage * damageMultiplier,
                    radius = weapon.bulletRadius,
                    color = if (isQuad) NeonOrange else weapon.bulletColor,
                    isRocket = weapon.isRocket,
                    isPiercing = weapon.isPiercing,
                    isFlame = weapon.isFlame
                )
            )
        }

        // Muzzle particles
        createMuzzleFlash(originX, originY, angle, weapon.bulletColor)

        if (player.isLocal) {
            soundEngine.playShoot(weapon.id)
        }

        if (player.currentAmmo <= 0) {
            reloadPlayer(player)
        }
    }

    fun reloadPlayer(player: Player) {
        if (!player.isReloading && player.currentAmmo < player.weapon.maxAmmo) {
            player.isReloading = true
            player.reloadProgress = 0f
            addFloatingText("RELOADING...", player.x, player.y - 25f, TextSecondary)
        }
    }

    fun triggerDash(player: Player) {
        if (!player.isAlive || player.dashCooldownRemaining > 0f) return
        player.isDashing = true
        player.dashCooldownRemaining = 2.5f
        soundEngine.playDash()
        createDashParticles(player.x, player.y, player.avatarColor)
        // Reset dash speed after short duration
        kotlin.concurrent.thread {
            Thread.sleep(180)
            player.isDashing = false
        }
    }

    private fun updateBullets(dt: Float) {
        val bulletIterator = bullets.iterator()
        while (bulletIterator.hasNext()) {
            val bullet = bulletIterator.next()
            bullet.lifeSpan -= dt
            if (bullet.lifeSpan <= 0f) {
                if (bullet.isRocket) explodeRocket(bullet.x, bullet.y, bullet.shooterId, bullet.shooterName, bullet.damage)
                bulletIterator.remove()
                continue
            }

            bullet.x += bullet.vx
            bullet.y += bullet.vy

            // Check world bounds
            if (bullet.x < 20f || bullet.x > worldWidth - 20f || bullet.y < 20f || bullet.y > worldHeight - 20f) {
                if (bullet.isRocket) explodeRocket(bullet.x, bullet.y, bullet.shooterId, bullet.shooterName, bullet.damage)
                createHitSpark(bullet.x, bullet.y, bullet.color)
                bulletIterator.remove()
                continue
            }

            // Check obstacle collision
            var hitObstacle = false
            for (obs in obstacles) {
                if (circleIntersectsRect(bullet.x, bullet.y, bullet.radius, obs.x, obs.y, obs.width, obs.height)) {
                    hitObstacle = true
                    if (obs.isDestructible) {
                        obs.health -= bullet.damage
                        if (obs.health <= 0f) {
                            obstacles.remove(obs)
                            createExplosion(obs.x + obs.width / 2f, obs.y + obs.height / 2f, 30, obs.color)
                            soundEngine.playExplosion()
                            // 50% chance to drop powerup
                            if (Random.nextBoolean()) {
                                spawnPowerUp(PowerUpType.values().random(), obs.x + obs.width / 2f, obs.y + obs.height / 2f)
                            }
                        }
                    }
                    break
                }
            }

            if (hitObstacle) {
                if (bullet.isRocket) explodeRocket(bullet.x, bullet.y, bullet.shooterId, bullet.shooterName, bullet.damage)
                createHitSpark(bullet.x, bullet.y, bullet.color)
                bulletIterator.remove()
                continue
            }

            // Check Player Collisions
            var bulletConsumed = false
            for (victim in players) {
                if (victim.id == bullet.shooterId || !victim.isAlive) continue
                val pRadius = if (victim.isBotBoss) 30f else 18f
                if (hypot(bullet.x - victim.x, bullet.y - victim.y) < pRadius + bullet.radius) {
                    val shooter = players.find { it.id == bullet.shooterId }
                    val isDead = damagePlayer(victim, bullet.damage, shooter, bullet.isRocket)
                    if (bullet.isRocket) {
                        explodeRocket(bullet.x, bullet.y, bullet.shooterId, bullet.shooterName, bullet.damage)
                    } else {
                        createHitSpark(bullet.x, bullet.y, bullet.color)
                    }

                    if (!bullet.isPiercing) {
                        bulletConsumed = true
                        break
                    }
                }
            }

            if (bulletConsumed) {
                bulletIterator.remove()
            }
        }
    }

    private fun explodeRocket(x: Float, y: Float, shooterId: String, shooterName: String, damage: Float) {
        val radius = 120f
        soundEngine.playExplosion()
        createExplosion(x, y, 40, NeonRed)

        val shooter = players.find { it.id == shooterId }
        for (victim in players) {
            if (!victim.isAlive) continue
            val dist = hypot(victim.x - x, victim.y - y)
            if (dist < radius) {
                val falloff = (1f - dist / radius).coerceIn(0.2f, 1f)
                damagePlayer(victim, damage * falloff, shooter, true)
            }
        }
    }

    fun damagePlayer(victim: Player, amount: Float, shooter: Player?, isCrit: Boolean = false): Boolean {
        if (!victim.isAlive) return false

        var remainingDamage = amount
        if (victim.shield > 0f) {
            val shieldDmg = min(victim.shield, remainingDamage)
            victim.shield -= shieldDmg
            remainingDamage -= shieldDmg
            if (victim.shield <= 0f) {
                soundEngine.playShieldBreak()
                addFloatingText("SHIELD BROKEN!", victim.x, victim.y - 30f, ShieldBlue)
            }
        }

        if (remainingDamage > 0f) {
            victim.health -= remainingDamage
        }

        // Damage indicator
        val textColor = if (isCrit) NeonOrange else NeonRed
        val label = if (isCrit) "-${amount.toInt()} CRIT!" else "-${amount.toInt()}"
        addFloatingText(label, victim.x + (Random.nextFloat() - 0.5f) * 20f, victim.y - 15f, textColor)

        if (victim.isLocal) {
            soundEngine.playHit()
        }

        if (victim.health <= 0f) {
            killPlayer(victim, shooter)
            return true
        }
        return false
    }

    private fun killPlayer(victim: Player, killer: Player?) {
        victim.isAlive = false
        victim.health = 0f
        victim.deaths++
        victim.respawnTimer = if (gameMode == GameMode.BOT_SURVIVAL) 0f else 3.5f

        createExplosion(victim.x, victim.y, 45, victim.avatarColor)
        soundEngine.playExplosion()

        val killerName = killer?.name ?: "Cyber Hazard"
        val weaponName = killer?.weapon?.name ?: "Combat Slug"

        if (killer != null) {
            killer.kills++
            killer.score += if (victim.isBotBoss) 500 else 100
            killer.comboCount++
            killer.comboTimer = 4.0f

            if (killer.comboCount >= 2) {
                val comboName = when (killer.comboCount) {
                    2 -> "DOUBLE KILL! 🔥"
                    3 -> "TRIPLE KILL! ⚡"
                    4 -> "RAMPAGE! 💥"
                    else -> "UNSTOPPABLE! 👑"
                }
                addFloatingText(comboName, killer.x, killer.y - 40f, NeonYellow)
            }
        }

        val event = KillEvent(
            id = killIdCounter++,
            killerName = killerName,
            victimName = victim.name,
            weaponName = weaponName,
            isLocalKiller = killer?.isLocal == true,
            isLocalVictim = victim.isLocal
        )
        killEvents.add(0, event)
        if (killEvents.size > 5) killEvents.removeLast()

        // Check Victory condition
        if (killer != null && targetKills > 0 && killer.kills >= targetKills) {
            endMatchByKills(killer)
        }
    }

    private fun respawnPlayer(player: Player) {
        player.isAlive = true
        player.health = player.maxHealth
        player.shield = player.maxShield
        player.currentAmmo = player.weapon.maxAmmo
        player.isReloading = false

        // Random respawn location safe from center
        val rx = if (Random.nextBoolean()) Random.nextFloat() * 250f + 80f else worldWidth - (Random.nextFloat() * 250f + 80f)
        val ry = Random.nextFloat() * (worldHeight - 200f) + 100f
        player.x = rx
        player.y = ry
        createMuzzleFlash(rx, ry, 0f, player.avatarColor)
    }

    private fun endMatchByKills(winner: Player) {
        isGameOver = true
        winnerPlayerName = winner.name
        isVictory = (winner.isLocal)
        if (isVictory) soundEngine.playVictory()
    }

    private fun endMatchByTime() {
        isGameOver = true
        val highest = players.maxByOrNull { it.kills }
        winnerPlayerName = highest?.name ?: localPlayerName
        isVictory = (highest?.isLocal == true)
        if (isVictory) soundEngine.playVictory()
    }

    fun sendQuickChatMessage(senderName: String, messageText: String) {
        chatMessages.add(ChatMessage(chatIdCounter++, senderName, messageText))
        if (chatMessages.size > 8) chatMessages.removeAt(0)
    }

    // Particle helpers
    private fun createMuzzleFlash(x: Float, y: Float, angle: Float, color: Color) {
        for (i in 0 until 6) {
            val speed = Random.nextFloat() * 6f + 2f
            val pAngle = angle + (Random.nextFloat() - 0.5f) * 0.8f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(pAngle) * speed,
                    vy = sin(pAngle) * speed,
                    color = color,
                    radius = Random.nextFloat() * 4f + 2f,
                    lifeSpan = 0.25f,
                    maxLifeSpan = 0.25f
                )
            )
        }
    }

    private fun createHitSpark(x: Float, y: Float, color: Color) {
        for (i in 0 until 8) {
            val speed = Random.nextFloat() * 8f + 2f
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    radius = Random.nextFloat() * 3f + 1.5f,
                    lifeSpan = 0.35f,
                    maxLifeSpan = 0.35f
                )
            )
        }
    }

    private fun createExplosion(x: Float, y: Float, count: Int, color: Color) {
        for (i in 0 until count) {
            val speed = Random.nextFloat() * 12f + 3f
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (Random.nextBoolean()) color else NeonOrange,
                    radius = Random.nextFloat() * 6f + 2f,
                    lifeSpan = Random.nextFloat() * 0.4f + 0.3f,
                    maxLifeSpan = 0.7f
                )
            )
        }
    }

    private fun createPowerUpBurst(x: Float, y: Float, type: PowerUpType) {
        val color = when (type) {
            PowerUpType.HEALTH_PACK -> HealthGreen
            PowerUpType.SHIELD_BOOST -> ShieldBlue
            PowerUpType.QUAD_DAMAGE -> NeonOrange
            PowerUpType.SPEED_OVERDRIVE -> NeonGreen
            PowerUpType.NUKE_BOMB -> NeonRed
        }
        createExplosion(x, y, 20, color)
    }

    private fun createDashParticles(x: Float, y: Float, color: Color) {
        for (i in 0 until 12) {
            particles.add(
                Particle(
                    x = x + (Random.nextFloat() - 0.5f) * 20f,
                    y = y + (Random.nextFloat() - 0.5f) * 20f,
                    vx = (Random.nextFloat() - 0.5f) * 3f,
                    vy = (Random.nextFloat() - 0.5f) * 3f,
                    color = color,
                    radius = Random.nextFloat() * 5f + 2f,
                    lifeSpan = 0.4f,
                    maxLifeSpan = 0.4f
                )
            )
        }
    }

    private fun addFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(
            FloatingText(
                id = textIdCounter++,
                text = text,
                x = x,
                y = y,
                color = color
            )
        )
    }

    private fun updateFX(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.lifeSpan -= dt
            if (p.lifeSpan <= 0f) {
                pIter.remove()
                continue
            }
            p.x += p.vx
            p.y += p.vy
            p.alpha = (p.lifeSpan / p.maxLifeSpan).coerceIn(0f, 1f)
        }

        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.lifeSpan -= dt
            if (t.lifeSpan <= 0f) {
                tIter.remove()
                continue
            }
            t.y += t.vy * dt
            t.alpha = (t.lifeSpan / 0.9f).coerceIn(0f, 1f)
        }
    }

    private fun circleIntersectsRect(cx: Float, cy: Float, cr: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        val closestX = cx.coerceIn(rx, rx + rw)
        val closestY = cy.coerceIn(ry, ry + rh)
        val distanceX = cx - closestX
        val distanceY = cy - closestY
        return (distanceX * distanceX + distanceY * distanceY) < (cr * cr)
    }
}
