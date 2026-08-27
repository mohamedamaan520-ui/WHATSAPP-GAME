package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.*
import com.example.model.GameMode
import com.example.model.WeaponId
import com.example.whatsapp.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScreenState {
    object MainLobby : ScreenState()
    data class OnlineLobby(val gameMode: GameMode) : ScreenState()
    data class ActiveGame(
        val gameMode: GameMode,
        val playerName: String,
        val opponentName: String,
        val roomCode: String,
        val targetKills: Int,
        val matchDurationSec: Int
    ) : ScreenState()
    object SplitScreen2P : ScreenState()
    object Armory : ScreenState()
    object WhatsAppHub : ScreenState()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GameDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameDao())

    val soundEngine = SoundEngine(application)

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.MainLobby)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _profile = MutableStateFlow(PlayerProfileEntity())
    val profile: StateFlow<PlayerProfileEntity> = _profile.asStateFlow()

    private val _matchRecords = MutableStateFlow<List<MatchRecordEntity>>(emptyList())
    val matchRecords: StateFlow<List<MatchRecordEntity>> = _matchRecords.asStateFlow()

    private val _whatsAppDuels = MutableStateFlow<List<WhatsAppDuelRecordEntity>>(emptyList())
    val whatsAppDuels: StateFlow<List<WhatsAppDuelRecordEntity>> = _whatsAppDuels.asStateFlow()

    private val _weaponUnlocks = MutableStateFlow<List<WeaponUnlockEntity>>(emptyList())
    val weaponUnlocks: StateFlow<List<WeaponUnlockEntity>> = _weaponUnlocks.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }

        viewModelScope.launch {
            repository.profileFlow.collect { p ->
                if (p != null) {
                    _profile.value = p
                    soundEngine.isSoundEnabled = p.soundEnabled
                    soundEngine.isVibrationEnabled = p.vibrationEnabled
                }
            }
        }

        viewModelScope.launch {
            repository.matchRecordsFlow.collect { list ->
                _matchRecords.value = list
            }
        }

        viewModelScope.launch {
            repository.whatsAppDuelsFlow.collect { list ->
                _whatsAppDuels.value = list
            }
        }

        viewModelScope.launch {
            repository.weaponUnlocksFlow.collect { list ->
                _weaponUnlocks.value = list
            }
        }
    }

    fun handleIntent(intent: Intent?) {
        val dataUri = intent?.dataString ?: intent?.getStringExtra(Intent.EXTRA_TEXT)
        val roomCode = WhatsAppHelper.extractRoomCodeFromUriOrText(dataUri)
        if (roomCode != null) {
            // Auto launch into Online WhatsApp Duel room!
            navigateToGame(
                gameMode = GameMode.ONLINE_WHATSAPP_DUEL,
                playerName = _profile.value.gamerTag,
                opponentName = "WhatsApp Friend",
                roomCode = roomCode,
                targetKills = 10,
                matchDurationSec = 180
            )
        }
    }

    fun navigateToLobby() {
        _screenState.value = ScreenState.MainLobby
    }

    fun navigateToOnlineLobby(mode: GameMode) {
        _screenState.value = ScreenState.OnlineLobby(mode)
    }

    fun navigateToGame(
        gameMode: GameMode,
        playerName: String,
        opponentName: String,
        roomCode: String = WhatsAppHelper.generateRoomCode(),
        targetKills: Int = 10,
        matchDurationSec: Int = 180
    ) {
        if (gameMode == GameMode.SPLIT_SCREEN_2P) {
            _screenState.value = ScreenState.SplitScreen2P
        } else {
            _screenState.value = ScreenState.ActiveGame(
                gameMode = gameMode,
                playerName = playerName,
                opponentName = opponentName,
                roomCode = roomCode,
                targetKills = targetKills,
                matchDurationSec = matchDurationSec
            )
        }
    }

    fun navigateToArmory() {
        _screenState.value = ScreenState.Armory
    }

    fun navigateToWhatsAppHub() {
        _screenState.value = ScreenState.WhatsAppHub
    }

    fun toggleSound() {
        val current = _profile.value
        val updated = current.copy(soundEnabled = !current.soundEnabled)
        _profile.value = updated
        soundEngine.isSoundEnabled = updated.soundEnabled
        viewModelScope.launch { repository.updateProfile(updated) }
    }

    fun toggleVibration() {
        val current = _profile.value
        val updated = current.copy(vibrationEnabled = !current.vibrationEnabled)
        _profile.value = updated
        soundEngine.isVibrationEnabled = updated.vibrationEnabled
        viewModelScope.launch { repository.updateProfile(updated) }
    }

    fun selectWeapon(weaponId: WeaponId) {
        val current = _profile.value
        val updated = current.copy(selectedWeaponId = weaponId.name)
        _profile.value = updated
        viewModelScope.launch { repository.updateProfile(updated) }
    }

    fun unlockWeapon(weaponId: WeaponId, cost: Int) {
        viewModelScope.launch {
            repository.unlockOrUpgradeWeapon(weaponId, cost)
        }
    }

    fun updateGamerTag(newTag: String) {
        val current = _profile.value
        val updated = current.copy(gamerTag = newTag)
        _profile.value = updated
        viewModelScope.launch { repository.updateProfile(updated) }
    }

    fun onMatchFinished(gameMode: GameMode, kills: Int, score: Int, isWin: Boolean, opponent: String) {
        val earnedCredits = kills * 25 + if (isWin) 100 else 25
        val earnedXp = kills * 30 + if (isWin) 150 else 50
        viewModelScope.launch {
            repository.addCreditsAndXp(earnedCredits, earnedXp, kills, isWin)
            repository.recordMatch(gameMode, kills, if (isWin) 0 else 1, score, isWin, opponent)
            if (gameMode == GameMode.ONLINE_WHATSAPP_DUEL) {
                repository.recordWhatsAppDuelResult(opponent, isWin)
            }
        }
    }
}
