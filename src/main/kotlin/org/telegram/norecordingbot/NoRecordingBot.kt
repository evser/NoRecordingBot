package org.telegram.norecordingbot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.norecordingbot.handler.BannedUserMessageHandler
import org.telegram.norecordingbot.handler.RecordingMessageHandler
import org.telegram.norecordingbot.handler.TextMessageHandler
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException

@SpringBootApplication
@EnableScheduling
open class NoRecordingBot : TelegramLongPollingBot() {

    override fun getBotUsername(): String {
        return "NoRecordingBot"
    }

    override fun onUpdateReceived(e: Update) {
        val message = e.message ?: return

        val from = message.from
        if (from.bot!!) {
            return
        }

        val messageHandler =
                when {
                    BannedUserMessageHandler.shouldBeUsed(from) -> BannedUserMessageHandler(this, message)
                    TextMessageHandler.shouldBeUsed(message) -> TextMessageHandler(this, message)
                    RecordingMessageHandler.shouldBeUsed(message) -> RecordingMessageHandler(this, message)
                    else -> null
                }
        messageHandler?.handle()
    }


    override fun getBotToken(): String {
        return System.getenv("BOT_KEY")
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            println("Start!")
            ApiContextInitializer.init()
            try {
                TelegramBotsApi().registerBot(NoRecordingBot())
            } catch (ex: TelegramApiException) {
                ex.printStackTrace()
            }

            SpringApplication.run(NoRecordingBot::class.java, *args)
        }


    }

}
