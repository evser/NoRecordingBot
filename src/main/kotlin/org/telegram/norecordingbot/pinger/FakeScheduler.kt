package org.telegram.norecordingbot.pinger

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class FakeScheduler {

    @Scheduled(fixedRate = 1000 * 60 * 10)
    fun fakeRequestSender() {
        try {
            RestTemplate().getForObject(BASE_URL, Any::class.java)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    companion object {

        private const val BASE_URL = "https://no-recording-bot.herokuapp.com/"
    }
}
