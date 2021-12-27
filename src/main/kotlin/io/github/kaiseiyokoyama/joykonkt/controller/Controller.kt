package io.github.kaiseiyokoyama.joykonkt.controller

import io.github.kaiseiyokoyama.joykonkt.controller.report.output.PlayerLight
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.Rumble
import org.hid4java.HidDevice

interface Controller {
    var globalPacketNumber: UInt

    companion object {
        const val VENDOR: Int = 1406
        const val MAX_REPORT_LEN: Int = 362
    }

    fun init() {
        // enable IMU
        sendSubCommand(SubCommand.EnableIMU, arrayOf(0x01))
        // enable vibration
        sendSubCommand(SubCommand.EnableVibration, arrayOf(0x01))
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
