package org.telegram.norecordingbot.handler.data

object GifTriggerData {

    val TRIGGER_WORDS = listOf(
            "@NoRecordingBot",
            "NoRecordingBot",
            "Bot",
            "бот")

    val EXCLUDED_WORDS = listOf(
            "ботв",
            "забот",
            "робот",
            "работ",
            "http",
            "суббот",
            "хобот")

    val KISS_SMILES = listOf(
            "😘",
            "😙",
            "😚",
            "😍",
            "💋",
            "😽",
            "😗")
}
