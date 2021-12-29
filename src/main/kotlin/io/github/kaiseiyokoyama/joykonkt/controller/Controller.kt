package io.github.kaiseiyokoyama.joykonkt.controller

import io.github.kaiseiyokoyama.joykonkt.controller.report.input.SPIFlashRead
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.StandardFullReport
import io.github.kaiseiyokoyama.joykonkt.controller.report.input.calibration.Sticks
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.PlayerLight
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.Rumble
import org.hid4java.HidDevice
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Controller {
    var globalPacketNumber: UInt
    var factoryCalibration: Sticks
//    var userCalibration: Sticks

    companion object {
        const val VENDOR: Int = 1406
        const val MAX_REPORT_LEN: Int = 362
    }

    fun init() {
        // enable IMU
        sendSubCommand(SubCommand.EnableIMU, arrayOf(0x01))
        // enable vibration
        sendSubCommand(SubCommand.EnableVibration, arrayOf(0x01))

        // get calibrations
        spiFlashRead(0x603Du, 18u)?.let { spi ->
            Sticks.parse(spi.data)?.let {
                factoryCalibration = it
            }
        }

//        spiFlashRead(0x8012u, 20u)?.let { spi ->
//            val data = spi.data
//            println(spi)
//            println(spi.data.contentToString())
//            data.removeAt(9)
//            data.removeAt(9)
//            Sticks.parse(data.toByteArray())?.let {
//                userCalibration = it
//            }
//        }
    }

    fun setNonBlocking(nonBlocking: Boolean)

    fun read(timeoutMillis: Int?): Result<ByteArray>

//    fun read(data: ByteArray): Int
//
//    fun read(amount: Int): ByteArray
//
//    fun read(amount: Int, timeoutMillis: Int): ByteArray
//
//    fun read(bytes: ByteArray, timeoutMillis: Int): Int

//    fun getFeatureReport(data: ByteArray, reportId: Byte): Int

//    fun sendFeatureReport(data: ByteArray, reportId: Byte): Int

    fun getIndexedString(index: Int): String

//    fun write(message: ByteArray, packetLength: Int, reportId: Byte): Int

    fun sendSubCommand(subCommand: SubCommand, argument: Array<Byte> = arrayOf()): Int

    fun rumble(l: Rumble, r: Rumble): Int

    fun setPlayerLights(
        led0: PlayerLight, // Closest led to SL Button
        led1: PlayerLight,
        led2: PlayerLight,
        led3: PlayerLight, // Closest led to SR Button
    ): Int = sendSubCommand(SubCommand.SetPlayerLights, arrayOf(PlayerLight.decode(led0, led1, led2, led3).toByte()))

    fun spiFlashRead(
        address: UInt,
        length: UByte,
        read: UInt = 50u,
        timeoutMillis: Int? = null,
    ): SPIFlashRead? {
        val buffer = ByteBuffer.allocate(UInt.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(address.toInt())

        val array = arrayOf(
            *buffer.array().toTypedArray(), // address
            length.toByte(), // length
        )
        sendSubCommand(
            SubCommand.SPIFlashRead,
            array
        )

        for (_i in 0u until read) {
            read(timeoutMillis).onSuccess {
                StandardFullReport.parse(it).onSuccess { report ->
                    when (report.payLoad) {
                        is StandardFullReport.PayLoad.SubCommandReply ->
                            if (report.payLoad.subCommand == SubCommand.SPIFlashRead) {
                                return SPIFlashRead.parse(
                                    report.payLoad.data
                                )
                            }
                        else -> {
                            // do nothing
                        }
                    }
                }
            }
        }

        return null
    }

    fun devices(): Array<HidDevice>

    fun close()
}

internal fun Controller.subCommandMessage(
    lRumble: Rumble? = null,
    rRumble: Rumble? = null,
    subCommand: SubCommand,
    argument: Array<Byte> = arrayOf(),
): ByteArray {
    globalPacketNumber++

    if (globalPacketNumber >= 0x100000000000000u) {
        globalPacketNumber = 0u
    }

    return arrayOf(
        globalPacketNumber.and(0x0Fu)
            .toByte(), //GlobalPacketNumber; // Increment by 1 for each packet sent. It loops in 0x0 - 0xF range.
        *lRumble?.encode()?.toTypedArray() ?: arrayOf(0x00, 0x00, 0x00, 0x00),
        *rRumble?.encode()?.toTypedArray() ?: arrayOf(0x00, 0x00, 0x00, 0x00),
        subCommand.byte,
        *argument
    ).toByteArray()
}
