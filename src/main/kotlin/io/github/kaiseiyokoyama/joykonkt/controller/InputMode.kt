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

        override fun read(timeoutMillis: Int?): Result<ByteArray> = controller.read(timeoutMillis)

        override fun receive(timeoutMillis: Int?): Result<NormalReport> {
            read(timeoutMillis)
                .onFailure {
                    return Result.failure(it)
                }
                .onSuccess {
                    return NormalReport.parse(it)
                }

            TODO("Unreachable")
        }

        override fun unwrap() = controller
    }

    class StandardFullMode(
        val controller: Controller,
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

        override fun receive(timeoutMillis: Int?): Result<StandardFullReport> {
            read(timeoutMillis)
                .onFailure {
                    return Result.failure(it)
                }
                .onSuccess {
                    return StandardFullReport.parse(it)
                }

            TODO("Unreachable")
        }

        override fun unwrap(): Controller = controller
    }
}