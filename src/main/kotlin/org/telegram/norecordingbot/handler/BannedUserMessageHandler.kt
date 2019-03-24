package org.telegram.norecordingbot.handler

import org.telegram.norecordingbot.handler.data.UserData
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot

class BannedUserMessageHandler(bot: TelegramLongPollingBot, message: Message) : MessageHandler(bot, message) {

    override fun handle() {
        deleteMessage(message.chatId, message.messageId)
    }

    companion object {
        fun shouldBeUsed(from: User): Boolean {
            return UserData.bannedUsers.contains(from.id)
                    || UserData.bannedUsersByName.values.stream()
                    .flatMap(MutableList<String>::stream)
                    .anyMatch { chunk -> (from.firstName + from.lastName + from.userName).contains(chunk, ignoreCase = true) }
        }
    }
}
