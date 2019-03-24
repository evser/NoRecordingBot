package org.telegram.norecordingbot.handler

import org.telegram.norecordingbot.GiphyTelegram
import org.telegram.norecordingbot.handler.data.UserData
import org.telegram.telegrambots.api.methods.send.SendDocument
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import java.util.*

abstract class MessageHandler(val bot: TelegramLongPollingBot, val message: Message) {

    val from: User = message.from

    abstract fun handle()

    protected fun deleteMessage(chatId: Long, messageId: Int) {
        val deleteMessage = DeleteMessage(chatId, messageId)
        try {
            bot.execute(deleteMessage)
        } catch (ex: TelegramApiException) {
            ex.printStackTrace()
        }
    }

    protected fun sendMessage(chatId: Long, text: String): Message? {
        val sendMessage = SendMessage(chatId, text)
        try {
            return bot.execute(sendMessage)
        } catch (ex: TelegramApiException) {
            ex.printStackTrace()
        }
        return Message()
    }

    protected fun sendMessage(text: String): Message? {
        return sendMessage(message.chatId, text)
    }

    protected fun sendGif(text: String) {
        try {
            val giphy = GiphyTelegram(UserData.GIPHY_KEY)
            val searchFeed = giphy.search(text, 10, Random().nextInt(10))
            if (!searchFeed.dataList.isEmpty()) {
                val gifLocation = searchFeed.dataList[Random().nextInt(searchFeed.dataList.size)]
                        .images
                        .original
                        .url

                val sendDocument = SendDocument()
                sendDocument.setChatId(message.chatId!!)
                sendDocument.document = gifLocation
                bot.sendDocument(sendDocument)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
}
