package org.telegram.norecordingbot.handler

import org.telegram.norecordingbot.handler.data.Commands
import org.telegram.norecordingbot.handler.data.GifTriggerData
import org.telegram.norecordingbot.handler.data.SalutationData
import org.telegram.norecordingbot.handler.data.UserData
import org.telegram.norecordingbot.handler.data.UserData.BAN_USER_SECONDS
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class TextMessageHandler(bot: TelegramLongPollingBot, message: Message) : MessageHandler(bot, message) {

    override fun handle() {
        val text = message.text ?: message.sticker.emoji

        val command = text.replace("@${bot.botUsername}", "")
        if (command.startsWith(Commands.START)) {
            sendMessage("This bot removes voice and video recordings from group chats. Add me to any chat and enjoy!")
        } else if (command.startsWith(Commands.REFILL)) {
            val refillDate = UserData.userRefill[from.id]
            if (refillDate == null || refillDate.isBefore(LocalDateTime.now().minusMinutes(UserData.EXCUSE_REFILL_PERIOD))) {
                UserData.userRefill[from.id] = LocalDateTime.now()
                val userOperations = UserData.allowedOperationsPerDay[from.id]
                UserData.allowedOperationsPerDay[from.id] = userOperations?.copy(second = UserData.REFILL_COUNT) ?: Pair(LocalDateTime.now(), UserData.REFILL_COUNT)
                sendMessage("You have %d additional attempt(-s).".format(UserData.REFILL_COUNT))
            } else {
                sendMessage("Next refill is available on " + ZonedDateTime.of(refillDate, ZoneId.systemDefault()).plusMinutes(UserData.EXCUSE_REFILL_PERIOD)
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z")))

            }
        } else if (command.startsWith(Commands.ULT)) {
            ultimateGame()
        } else if (command.startsWith(Commands.BAN)) {
            val banCommand = command.split(" ")
            if (banCommand.size > 1) {
                banUser(banCommand[1])
            }
        } else {
            if (countTriggers(GifTriggerData.TRIGGER_WORDS, text) > countTriggers(GifTriggerData.EXCLUDED_WORDS, text)
                    || Random().nextInt(60) == 0) {
                var gifText = text
                for (word in GifTriggerData.TRIGGER_WORDS) {
                    gifText = gifText.replace(word, "", ignoreCase = true).trim()
                }
                sendGif(gifText)
            } else if (shouldTriggerOn(GifTriggerData.KISS_SMILES, text)) {
                if (Random().nextBoolean()) {
                    sendGif(text)
                } else {
                    sendGif(String(Base64.getDecoder().decode("Z2F5DQo=")))
                }
            } else if (SalutationData.SALUTATIONS.any
                    { trigger -> text.contains(".*\\b$trigger\\b.*".toRegex(RegexOption.IGNORE_CASE)) }) {
                sendMessage(SalutationData.SALUTATIONS[Random().nextInt(SalutationData.SALUTATIONS.size)])
            }
        }
    }

    private fun ultimateGame() {
        if (!ultUsers.add(from.id)) {
            return
        }
        thread {
            if (Random().nextBoolean()) {
                banCurrentUser()
            } else {
                sendMessage("You have 10 min power to ban anyone! Use /ban <user_chunk>")
                UserData.banAdminUser.add(from.id)
                Timer().schedule(10 * 60 * 1000) {
                    UserData.banAdminUser.remove(from.id)
                    UserData.bannedUsersByName.remove(from.id)
                }
            }
            ultUsers.remove(from.id)
        }
    }


    private fun banUser(userNameChunk: String) {
        if (UserData.banAdminUser.contains(from.id)) {
            UserData.bannedUsersByName.add(from.id, userNameChunk)
            sendMessage("$userNameChunk is banned for $BAN_USER_SECONDS seconds")
            Timer().schedule(BAN_USER_SECONDS * 1000L) {
                UserData.bannedUsersByName[from.id]?.remove(userNameChunk)
            }
        }
    }

    private fun banCurrentUser() {
        if (UserData.bannedUsers.add(from.id)) {
            sendMessage("You are banned for $BAN_USER_SECONDS seconds")
            sendGif("ban")
        } else {
            sendMessage("Already banned");
        }
        Timer().schedule(BAN_USER_SECONDS * 1000L) {
            UserData.bannedUsers.remove(from.id)
        }
    }


    private fun shouldTriggerOn(triggers: List<String>, text: String): Boolean {
        return triggers
                .any { trigger -> text.contains(trigger, true) }
    }

    private fun countTriggers(triggers: List<String>, text: String): Int {
        return triggers
                .map { trigger -> text.split(trigger, ignoreCase = true).size - 1 }
                .sum()
    }

    companion object {
        val ultUsers = ConcurrentSkipListSet<Int>()

        fun shouldBeUsed(message: Message): Boolean {
            return message.hasText() || message.sticker != null
        }
    }
}
