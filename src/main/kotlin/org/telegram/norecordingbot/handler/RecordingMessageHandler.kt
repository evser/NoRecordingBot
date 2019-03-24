package org.telegram.norecordingbot.handler

import org.springframework.core.io.ClassPathResource
import org.telegram.norecordingbot.handler.data.UserData
import org.telegram.telegrambots.api.methods.send.SendVoice
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RecordingMessageHandler(bot: TelegramLongPollingBot, message: Message) : MessageHandler(bot, message) {
    override fun handle() {

        val videoDetected = message.videoNote != null
        val voiceDetected = message.voice != null

        if (videoDetected || voiceDetected) {
            checkAllowedOperations(from, UserData.allowedOperationsPerDay, UserData.EXCUSE_PERIOD_IN_MINUTES, UserData.ALLOWED_VOICE_MSGS)

            val userOperations = UserData.allowedOperationsPerDay[from.id]
            userOperations?.second?.let {
                if (it < 0) {
                    deleteMessage(message.chatId, message.messageId)

                    if (userOperations.second > -UserData.USER_FULL_BAN_MESSAGE_LIMIT) {
                        sendMessage(String.format("%s of %s has been deleted.", if (voiceDetected) "Voice" else "Video Note", getUserName(from)))
                    } else if (userOperations.second == -UserData.USER_FULL_BAN_MESSAGE_LIMIT) {
                        sendMessage("%s will remain silent until %s.".format(
                                getUserName(from),
                                ZonedDateTime.of(userOperations.first, ZoneId.systemDefault()).plusMinutes(UserData.EXCUSE_PERIOD_IN_MINUTES)
                                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z"))))
                        insultSender(message)
                    }
                    UserData.allowedOperationsPerDay[from.id] = userOperations.copy(second = userOperations.second - 1)
                } else {
                    sendMessage("${getUserName(from)} has ${if (it == 0) "no" else it.toString()} more attempt(-s) to sin today again so much.")
                }
            }
        }
    }

    private fun insultSender(message: Message) {
        try {
            val sendVoice = SendVoice()
            sendVoice.setNewVoice("voice", ClassPathResource("reply.ogg").inputStream)
            sendVoice.setChatId(message.chatId)
            bot.sendVoice(sendVoice)
        } catch (ex: TelegramApiException) {
            ex.printStackTrace()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }

    private fun checkAllowedOperations(from: User, allowedOperations: MutableMap<Int, Pair<LocalDateTime, Int>>, excusePeriodInMinutes: Long, maxAllowedOperations: Int) {
        val userOperations = allowedOperations[from.id]
        if (userOperations == null || userOperations.first.isBefore(LocalDateTime.now().minusMinutes(excusePeriodInMinutes))) {
            allowedOperations[from.id] = Pair(LocalDateTime.now(), maxAllowedOperations - 1)
        } else if (userOperations.second >= 0) {
            allowedOperations[from.id] = userOperations.copy(second = userOperations.second - 1)
        }
    }

    private fun getUserName(user: User): String {
        val userName = user.userName
        return when {
            userName != null -> "@$userName"
            else -> "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}"
        }
    }

    companion object {
        fun shouldBeUsed(message: Message): Boolean {
            return message.voice ?: message.videoNote != null
        }
    }
}
