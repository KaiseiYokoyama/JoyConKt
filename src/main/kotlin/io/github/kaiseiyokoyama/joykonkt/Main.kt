package io.github.kaiseiyokoyama.joykonkt

import io.github.kaiseiyokoyama.joykonkt.controller.Controller
import kotlinx.coroutines.channels.Channel
import java.util.*

fun main() {
    val ach = Channel<Controller>()
    Manager(ach, Channel())

    var controller: Controller? = null

    while (true) {
        val tryResult = ach.tryReceive().getOrNull()
        if (tryResult != null) {
            controller = tryResult
            break
        } else {
            Thread.sleep(160)
        }
    }

//    controller!!.setNonBlocking(true)

    while (true) {
        println(controller!!.read().contentToString())
        Thread.sleep(160)
    }
}