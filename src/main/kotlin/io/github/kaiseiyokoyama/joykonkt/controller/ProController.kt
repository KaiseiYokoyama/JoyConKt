package io.github.kaiseiyokoyama.joykonkt.controller

import io.github.kaiseiyokoyama.joykonkt.controller.report.input.calibration.Sticks
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.ID
import io.github.kaiseiyokoyama.joykonkt.controller.report.output.Rumble
import org.hid4java.HidDevice
import java.lang.RuntimeException

class ProController private constructor(
    private val hid: HidDevice,
) : Controller {
    override lateinit var factoryCalibration: Sticks
//    override lateinit var userCalibration: Sticks

    companion object {
        /**
         * プロダクトID
         */
        const val PRODUCT: Int = 8201

        /**
         * Try to create instance
         */
        fun tryNew(hid: HidDevice): ProController? {
            return when (Pair(hid.vendorId, hid.productId)) {
                Pair(Controller.VENDOR, PRODUCT) -> {
                    ProController(hid)
                }
                else -> null
            }
        }
    }

    init {
        if (!hid.isOpen) hid.open()

        init()
    }

    val serialNumber = hid.serialNumber
    val releaseNumber = hid.releaseNumber

    override fun toString(): String {
        return "Pro Controller $hid"
    }

    override var globalPacketNumber: UInt = 0u

    // -- Implementation of Controller interface --

    override fun setNonBlocking(nonBlocking: Boolean) = hid.setNonBlocking(nonBlocking)

    override fun read(timeoutMillis: Int?): Result<ByteArray> {
        return try {
            Result.success(
                if (timeoutMillis != null) {
                    hid.read(Controller.MAX_REPORT_LEN, timeoutMillis).toByteArray()
                } else {
                    hid.read().toByteArray()
                }
            )
        } catch (e: NegativeArraySizeException) {
            Result.failure(e)
        }
    }

    override fun sendSubCommand(subCommand: SubCommand, argument: Array<Byte>): Int {
        val message = subCommandMessage(subCommand = subCommand, argument = argument)

        return hid.write(
            message,
            message.size,
            ID.RumbleSubCommand.intValue.toByte(),
//            subCommand.byte
        )
    }

    override fun rumble(l: Rumble, r: Rumble): Int {
        val subCommand = SubCommand.GetControllerState
        val message = subCommandMessage(l, r, subCommand)

        return hid.write(
            message,
            message.size,
            ID.RumbleSubCommand.intValue.toByte(),
        )
    }

    override fun getIndexedString(index: Int): String = hid.getIndexedString(index)

    override fun devices(): Array<HidDevice> = arrayOf(hid)

    override fun close() = hid.close()

//    override fun read() = hid.read().toByteArray()
//
//    override fun read(data: ByteArray): Int = hid.read(data)
//
//    override fun read(amount: Int): ByteArray = hid.read(amount).toByteArray()
//
//    override fun read(amount: Int, timeoutMillis: Int): ByteArray = hid.read(amount, timeoutMillis).toByteArray()
//
//    override fun read(bytes: ByteArray, timeoutMillis: Int): Int = hid.read(bytes, timeoutMillis)
//
//    override fun getFeatureReport(data: ByteArray, reportId: Byte): Int = hid.getFeatureReport(data, reportId)
//
//    override fun sendFeatureReport(data: ByteArray, reportId: Byte): Int = hid.sendFeatureReport(data, reportId)
//    override fun write(message: ByteArray, packetLength: Int, reportId: Byte): Int = hid.write(message, packetLength, reportId)
}