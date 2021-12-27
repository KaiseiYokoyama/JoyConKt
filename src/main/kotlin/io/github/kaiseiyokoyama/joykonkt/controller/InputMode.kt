package io.github.kaiseiyokoyama.joykonkt.controller

import io.github.kaiseiyokoyama.joykonkt.controller.report.input.ID
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.InputReport
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.NormalReport
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.StandardFullReport

sealed interface InputMode<T : InputReport> {
    fun init(controller: Controller)

    fun read(timeoutMillis: Int?): Result<ByteArray>

    fun receive(timeoutMillis: Int? = null): Result<T>

    fun unwrap(): Controller

    /**
     * WIP especially for Pro Controller
     */
    class NormalMode<C : Controller>(
        val controller: C
    ) : InputMode<NormalReport> {
        init {
            init(controller)
        }

        override fun init(controller: Controller) {
            // change mode
            controller.sendSubCommand(SubCommand.SetInputReportMode, arrayOf(0x3F))
        }

        override fun read(timeoutMillis: Int?): Result<ByteArray> = controller.read(timeoutMillis)

        override fun receive(timeoutMillis: Int?): Result<NormalReport> =
            read(timeoutMillis).fold(
                onSuccess = { NormalReport.parse(it) },
                onFailure = { Result.failure(it) }
            )

        override fun unwrap() = controller
    }

    class StandardFullMode<C : Controller>(
        val controller: C,
        private val inputReportID: ID = ID.StandardFullMode,
    ) : InputMode<StandardFullReport> {
        init {
            init(controller)
        }

        override fun init(controller: Controller) {
            // change mode
            controller.sendSubCommand(SubCommand.SetInputReportMode, arrayOf(0x30))
        }

        override fun read(timeoutMillis: Int?): Result<ByteArray> = controller.read(timeoutMillis)

        override fun receive(timeoutMillis: Int?): Result<StandardFullReport> =
            read(timeoutMillis).fold(
                onSuccess = {
                    StandardFullReport.parse(it)
                },
                onFailure = {
                    Result.failure(it)
                }
            )

        override fun unwrap(): Controller = controller
    }
}