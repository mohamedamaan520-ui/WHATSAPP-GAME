package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 'local_player'")
    fun getProfileFlow(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 'local_player'")
    suspend fun getProfile(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM match_records ORDER BY timestamp DESC LIMIT 20")
    fun getMatchRecordsFlow(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchRecord(record: MatchRecordEntity)

    @Query("SELECT * FROM whatsapp_duel_records ORDER BY lastPlayedDate DESC")
    fun getWhatsAppDuelsFlow(): Flow<List<WhatsAppDuelRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppDuel(duel: WhatsAppDuelRecordEntity)

    @Query("SELECT * FROM weapon_unlocks")
    fun getWeaponUnlocksFlow(): Flow<List<WeaponUnlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeaponUnlock(unlock: WeaponUnlockEntity)
}
