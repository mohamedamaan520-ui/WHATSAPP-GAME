package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.GameMode
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.*
import com.example.ui.theme.CyberDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle initial intent
        viewModel.handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberDark)
                ) {
                    CyberStrikeApp(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent)
    }
}

@Composable
fun CyberStrikeApp(viewModel: GameViewModel) {
    val screenState by viewModel.screenState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val matchRecords by viewModel.matchRecords.collectAsState()
    val whatsAppDuels by viewModel.whatsAppDuels.collectAsState()
    val weaponUnlocks by viewModel.weaponUnlocks.collectAsState()

    when (val state = screenState) {
        is ScreenState.MainLobby -> {
            MainLobbyScreen(
                profile = profile,
                onStartGame = { mode, player, opponent ->
                    viewModel.navigateToGame(mode, player, opponent)
                },
                onOpenOnlineLobby = { mode ->
                    viewModel.navigateToOnlineLobby(mode)
                },
                onOpenArmory = {
                    viewModel.navigateToArmory()
                },
                onOpenWhatsAppHub = {
                    viewModel.navigateToWhatsAppHub()
                },
                onToggleSound = {
                    viewModel.toggleSound()
                },
                onToggleVibration = {
                    viewModel.toggleVibration()
                }
            )
        }

        is ScreenState.OnlineLobby -> {
            OnlineMultiplayerLobbyScreen(
                gameMode = state.gameMode,
                playerName = profile.gamerTag,
                onStartMatch = { targetKills, durationSec, roomCode ->
                    viewModel.navigateToGame(
                        gameMode = state.gameMode,
                        playerName = profile.gamerTag,
                        opponentName = if (state.gameMode == GameMode.ONLINE_WHATSAPP_DUEL) "WhatsApp Opponent" else "Viper_99",
                        roomCode = roomCode,
                        targetKills = targetKills,
                        matchDurationSec = durationSec
                    )
                },
                onBack = {
                    viewModel.navigateToLobby()
                }
            )
        }

        is ScreenState.ActiveGame -> {
            ActiveGameScreen(
                gameMode = state.gameMode,
                playerName = state.playerName,
                opponentName = state.opponentName,
                roomCode = state.roomCode,
                targetKills = state.targetKills,
                matchDurationSec = state.matchDurationSec,
                soundEngine = viewModel.soundEngine,
                onGameOver = { kills, score, isWin, opponent ->
                    viewModel.onMatchFinished(state.gameMode, kills, score, isWin, opponent)
                },
                onExitGame = {
                    viewModel.navigateToLobby()
                }
            )
        }

        is ScreenState.SplitScreen2P -> {
            SplitScreenGameScreen(
                soundEngine = viewModel.soundEngine,
                onExitGame = {
                    viewModel.navigateToLobby()
                }
            )
        }

        is ScreenState.Armory -> {
            ArmoryScreen(
                profile = profile,
                weaponUnlocks = weaponUnlocks,
                onSelectWeapon = { weaponId ->
                    viewModel.selectWeapon(weaponId)
                },
                onUnlockWeapon = { weaponId, cost ->
                    viewModel.unlockWeapon(weaponId, cost)
                },
                onUpdateGamerTag = { newTag ->
                    viewModel.updateGamerTag(newTag)
                },
                onBack = {
                    viewModel.navigateToLobby()
                }
            )
        }

        is ScreenState.WhatsAppHub -> {
            WhatsAppDuelsHubScreen(
                duels = whatsAppDuels,
                matchRecords = matchRecords,
                onChallengeBuddy = { buddyName ->
                    viewModel.navigateToOnlineLobby(GameMode.ONLINE_WHATSAPP_DUEL)
                },
                onBack = {
                    viewModel.navigateToLobby()
                }
            )
        }
    }
}
