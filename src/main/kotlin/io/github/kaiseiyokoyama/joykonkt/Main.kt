package io.github.kaiseiyokoyama.joykonkt

import io.github.kaiseiyokoyama.joykonkt.controller.Controller
import io.github.kaiseiyokoyama.joykonkt.controller.InputMode
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.PlayerLight
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.Rumble

fun main() {
    val ach = Manager.attachedChannel

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

    val full = controller?.let { InputMode.StandardFullMode(it) }

    full?.let {
        it.controller.setPlayerLights(
            PlayerLight.Flash,
            PlayerLight.Flash,
            PlayerLight.Flash,
            PlayerLight.Flash,
        )

        println("F: ${it.controller.factoryCalibration}")
//        println("U: ${it.controller.userCalibration}")

        for (i_ in 0..20) {
            Thread.sleep(50)
            it.controller.rumble(
                Rumble(0.0, 0.0),
                Rumble(0.0, 0.0),
            )
        }
    }
}