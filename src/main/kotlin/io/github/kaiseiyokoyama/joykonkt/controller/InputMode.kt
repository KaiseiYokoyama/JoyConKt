package io.github.kaiseiyokoyama.joykonkt.controller

import io.github.kaiseiyokoyama.joykonkt.controller.report.input.ID
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.InputReport
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.NormalReport
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.StandardFullReport

sealed interface InputMode<T : InputReport> {
    fun init(controller: Controller)

    fun read(timeoutMillis: Int?): ByteArray

    fun receive(timeoutMillis: Int? = null): Result<T>

    fun unwrap(): Controller

    /**
     * WIP especially for Pro Controller
     */
    class NormalMode(
        val controller: Controller
    ) : InputMode<NormalReport> {
        init {
            init(controller)
        }

        override fun init(controller: Controller) {
            // change mode
            controller.sendSubCommand(SubCommand.SetInputReportMode, arrayOf(0x3F))
        }

        override fun read(timeoutMillis: Int?): ByteArray = controller.read(timeoutMillis)

        override fun receive(timeoutMillis: Int?): Result<NormalReport> =
            NormalReport.parse(read(timeoutMillis))

        override fun unwrap() = controller
    }

    class StandardFullMode(
        val controller: Controller,
        private val inputReportID: ID,
    ) : InputMode<StandardFullReport> {
        init {
            init(controller)
        }

        override fun init(controller: Controller) {
            // change mode
            controller.sendSubCommand(SubCommand.SetInputReportMode, arrayOf(0x30))
        }

        override fun read(timeoutMillis: Int?): ByteArray = controller.read(timeoutMillis)

        override fun receive(timeoutMillis: Int?): Result<StandardFullReport> =
            StandardFullReport.parse(read(timeoutMillis))

        override fun unwrap(): Controller = controller
    }
}