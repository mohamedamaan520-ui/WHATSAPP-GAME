package com.example.whatsapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.GameMode
import com.example.model.WhatsAppBuddy
import java.net.URLEncoder

object WhatsAppHelper {

    fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { chars.random() }.joinToString("")
        return "${code.substring(0, 3)}-${code.substring(3)}"
    }

    fun buildInviteMessage(roomCode: String, hostName: String, gameMode: GameMode): String {
        return """
🎮 *CYBERSTRIKE ONLINE ARENA CHALLENGE!* 💥

⚔️ *$hostName* challenged you to a LIVE SHOOTING DUEL!

🎯 *Room Code:* *$roomCode*
🔥 *Game Mode:* ${gameMode.displayName}
⚡ *Status:* LIVE MATCH WAITING FOR OPPONENT

📲 *How to Join:*
1. Open CyberStrike App
2. Tap "Join with Code" and enter: *$roomCode*
Or tap this link if installed:
cyberstrike://join?room=$roomCode

🏆 Ready for battle? Join now! 🚀
        """.trimIndent()
    }

    fun buildScoreChallengeMessage(playerName: String, kills: Int, score: Int, gameMode: GameMode, botWaves: Int = 0): String {
        val detail = if (gameMode == GameMode.BOT_SURVIVAL) "Surviving $botWaves Drone Waves!" else "Dominating the Cyber Arena!"
        return """
🏆 *CYBERSTRIKE BATTLE REPORT!* 🔫⚡

🔥 *$playerName* just scored *$score PTS* ($kills Kills) $detail

⚔️ Think you can beat my high score or duel me 1v1?

🎮 Download CyberStrike and challenge my squad now!
📲 cyberstrike://join?room=CHALLENGE
        """.trimIndent()
    }

    fun shareToWhatsApp(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed or package direct intent fails, use general Intent chooser
            try {
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share CyberStrike Room Challenge"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to open WhatsApp sharing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendDirectWhatsAppMessage(context: Context, phoneNumber: String, text: String) {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${URLEncoder.encode(text, "UTF-8")}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            shareToWhatsApp(context, text)
        }
    }

    fun copyToClipboard(context: Context, label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun extractRoomCodeFromUriOrText(input: String?): String? {
        if (input.isNullOrBlank()) return null

        // 1. Check URI format: cyberstrike://join?room=ABC-123 or https://cyberstrike.game/join?room=ABC-123
        try {
            val uri = Uri.parse(input)
            val param = uri.getQueryParameter("room")
            if (!param.isNullOrBlank()) {
                return param.uppercase().trim()
            }
        } catch (_: Exception) {}

        // 2. Check regex matching 6-char formatted room codes (e.g. ABC-123 or ABC123)
        val regex = Regex("([A-Za-z0-9]{3}-[A-Za-z0-9]{3})|([A-Za-z0-9]{6})")
        val match = regex.find(input)
        if (match != null) {
            var found = match.value.uppercase()
            if (found.length == 6 && !found.contains("-")) {
                found = "${found.substring(0, 3)}-${found.substring(3)}"
            }
            return found
        }
        return null
    }

    val DEFAULT_BUDDIES = listOf(
        WhatsAppBuddy("1", "Alex Hunter", "+1 (555) 234-8901", 12, 8, "Today"),
        WhatsAppBuddy("2", "Marcus 'Viper'", "+1 (555) 876-5432", 9, 11, "Yesterday"),
        WhatsAppBuddy("3", "Sarah Nova", "+1 (555) 349-1120", 15, 6, "2 days ago"),
        WhatsAppBuddy("4", "Ghost_99", "+1 (555) 432-9081", 7, 7, "3 days ago")
    )
}
