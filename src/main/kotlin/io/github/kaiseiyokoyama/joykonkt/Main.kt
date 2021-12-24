package io.github.kaiseiyokoyama.joykonkt

import io.github.kaiseiyokoyama.joykonkt.controller.Controller
import io.github.kaiseiyokoyama.joykonkt.controller.InputMode
import io.github.kaiseiyokoyama.joykonkt.controller.SubCommand
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.ID
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.Rumble
import kotlinx.coroutines.channels.Channel

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

    val normalModeController = InputMode.StandardFullMode(controller!!, ID.StandardFullMode)
//    controller!!.setNonBlocking(true)
//    controller!!.sendSubCommand(SubCommand.EnableVibration, arrayOf(0x01))
    println("Rumble")

    for (i in 0..40) {
//        normalModeController.receive()
//            .onSuccess {
//                println("Success: $it")
//            }
//            .onFailure {
//                println("Failure: $it")
//            }
        normalModeController.controller.rumble(
            Rumble(900.0, 0.4),
            Rumble(900.0, 0.4),
        )
        Thread.sleep(5)
//        normalModeController.controller.rumble(
//            Rumble(0.0, 0.0),
//            Rumble(0.0, 0.0),
//        )
//        Thread.sleep(400)
    }
    while (true) normalModeController.controller.rumble(
        Rumble(0.0, 0.0),
        Rumble(0.0, 0.0),
    )
}