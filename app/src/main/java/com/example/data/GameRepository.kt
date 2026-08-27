package com.example.data

import com.example.model.GameMode
import com.example.model.WeaponId
import com.example.whatsapp.WhatsAppHelper
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val profileFlow: Flow<PlayerProfileEntity?> = gameDao.getProfileFlow()
    val matchRecordsFlow: Flow<List<MatchRecordEntity>> = gameDao.getMatchRecordsFlow()
    val whatsAppDuelsFlow: Flow<List<WhatsAppDuelRecordEntity>> = gameDao.getWhatsAppDuelsFlow()
    val weaponUnlocksFlow: Flow<List<WeaponUnlockEntity>> = gameDao.getWeaponUnlocksFlow()

    suspend fun ensureInitialized() {
        val existing = gameDao.getProfile()
        if (existing == null) {
            gameDao.insertProfile(
                PlayerProfileEntity(
                    gamerTag = "CyberGhost",
                    credits = 500,
                    level = 1,
                    xp = 50
                )
            )
            // Seed initial weapon unlock
            gameDao.insertWeaponUnlock(WeaponUnlockEntity(WeaponId.ASSAULT_RIFLE.name, true, 1))

            // Seed initial WhatsApp duel buddies
            WhatsAppHelper.DEFAULT_BUDDIES.forEach { buddy ->
                gameDao.insertWhatsAppDuel(
                    WhatsAppDuelRecordEntity(
                        buddyName = buddy.name,
                        wins = buddy.wins,
                        losses = buddy.losses,
                        lastPlayedDate = buddy.lastPlayedDate
                    )
                )
            }
        }
    }

    suspend fun updateProfile(profile: PlayerProfileEntity) {
        gameDao.insertProfile(profile)
    }

    suspend fun addCreditsAndXp(earnedCredits: Int, earnedXp: Int, kills: Int, won: Boolean) {
        val current = gameDao.getProfile() ?: PlayerProfileEntity()
        val newXp = current.xp + earnedXp
        val newLevel = 1 + (newXp / 250)
        val updated = current.copy(
            credits = current.credits + earnedCredits,
            xp = newXp,
            level = newLevel,
            totalKills = current.totalKills + kills,
            totalWins = if (won) current.totalWins + 1 else current.totalWins
        )
        gameDao.insertProfile(updated)
    }

    suspend fun recordMatch(gameMode: GameMode, kills: Int, deaths: Int, score: Int, isWin: Boolean, opponentOrMap: String) {
        gameDao.insertMatchRecord(
            MatchRecordEntity(
                gameMode = gameMode.name,
                kills = kills,
                deaths = deaths,
                score = score,
                isWin = isWin,
                opponentOrMap = opponentOrMap
            )
        )
    }

    suspend fun recordWhatsAppDuelResult(buddyName: String, isWin: Boolean) {
        val all = WhatsAppHelper.DEFAULT_BUDDIES.find { it.name.equals(buddyName, ignoreCase = true) }
        val currentWins = all?.wins ?: 0
        val currentLosses = all?.losses ?: 0
        gameDao.insertWhatsAppDuel(
            WhatsAppDuelRecordEntity(
                buddyName = buddyName,
                wins = if (isWin) currentWins + 1 else currentWins,
                losses = if (!isWin) currentLosses + 1 else currentLosses,
                lastPlayedDate = "Just now"
            )
        )
    }

    suspend fun unlockOrUpgradeWeapon(weaponId: WeaponId, cost: Int) {
        val current = gameDao.getProfile() ?: return
        if (current.credits >= cost) {
            gameDao.insertProfile(current.copy(credits = current.credits - cost))
            gameDao.insertWeaponUnlock(WeaponUnlockEntity(weaponId.name, true, 1))
        }
    }
}
