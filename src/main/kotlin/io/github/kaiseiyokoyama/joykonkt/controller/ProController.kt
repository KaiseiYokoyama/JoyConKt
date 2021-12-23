package io.github.kaiseiyokoyama.joykonkt.controller

import org.hid4java.HidDevice

class ProController private constructor(
    private val hid: HidDevice
) : Controller {
    companion object {
        /**
         * プロダクトID
         */
        const val PRODUCT: Int = 8201

        /**
         * インスタンス作成
         */
        fun tryNew(hid: HidDevice): ProController? {
            return when (Pair(hid.vendorId, hid.productId)) {
                // ベンダーIDおよびプロダクトIDが正しい時
                Pair(Controller.VENDOR, PRODUCT) -> {
                    ProController(hid)
                }
                else -> null
            }
        }
    }

    init {
        if (!hid.isOpen) hid.open()
    }

    val serialNumber = hid.serialNumber
    val releaseNumber = hid.releaseNumber

    override fun toString(): String {
        return "Pro Controller $hid"
    }

    // -- Implementation of Controller interface --

    override fun setNonBlocking(nonBlocking: Boolean) = hid.setNonBlocking(nonBlocking)

    override fun read() = hid.read().toByteArray()

    override fun read(data: ByteArray): Int = hid.read(data)

    override fun read(amount: Int): ByteArray = hid.read(amount).toByteArray()

    override fun read(amount: Int, timeoutMillis: Int): ByteArray = hid.read(amount, timeoutMillis).toByteArray()

    override fun read(bytes: ByteArray, timeoutMillis: Int): Int = hid.read(bytes, timeoutMillis)

    override fun getFeatureReport(data: ByteArray, reportId: Byte): Int = hid.getFeatureReport(data, reportId)

    override fun sendFeatureReport(data: ByteArray, reportId: Byte): Int = hid.sendFeatureReport(data, reportId)

    override fun getIndexedString(index: Int): String = hid.getIndexedString(index)

    override fun write(message: ByteArray, packetLength: Int, reportId: Byte): Int = hid.write(message, packetLength, reportId)

    override fun devices(): Array<HidDevice> = arrayOf(hid)

    override fun close() = hid.close()
}