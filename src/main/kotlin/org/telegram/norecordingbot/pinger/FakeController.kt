package org.telegram.norecordingbot.pinger

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FakeController {

    @GetMapping("/")
    fun fakeMapping() {
        println("!!")
    }

    @GetMapping("/stop")
    fun stop(@RequestParam("password") password: String) {
        if ("b4fae45e62d0f1c85d12d84b55829b403e739e4401876bd54ffff98530c48f00b8e25b5925ba531b7809e201c9c8f5d42be02e5d709fc5cf5dd1bfa407867539" == DigestUtils.sha512Hex(password))
            println("Stopped remotely")
        System.exit(0)
    }
}
