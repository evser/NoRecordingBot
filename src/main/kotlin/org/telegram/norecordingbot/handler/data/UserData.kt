package org.telegram.norecordingbot.handler.data

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import javax.ws.rs.core.MultivaluedHashMap

object UserData {

    const val GIPHY_KEY = "4oEpeXjg6gQYX6Onnzrs38dfT0m9tgAZ"

    const val EXCUSE_PERIOD_IN_MINUTES = 24 * 60L

    const val ALLOWED_VOICE_MSGS = 2

    const val USER_FULL_BAN_MESSAGE_LIMIT = 3

    const val EXCUSE_REFILL_PERIOD = 2 * 24 * 60L

    const val REFILL_COUNT = 1

    const val BAN_USER_SECONDS = 60

    val allowedOperationsPerDay = ConcurrentHashMap<Int, Pair<LocalDateTime, Int>>()

    val userRefill = ConcurrentHashMap<Int, LocalDateTime>()

    val bannedUsers = ConcurrentSkipListSet<Int>()

    val bannedUsersByName = MultivaluedHashMap<Int, String>()

    val banAdminUser = HashSet<Int>()
}
