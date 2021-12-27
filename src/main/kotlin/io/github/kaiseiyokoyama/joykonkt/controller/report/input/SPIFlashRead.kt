package io.github.kaiseiyokoyama.joykonkt.controller.report.input

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class SPIFlashRead(
    val address: UInt,
    val length: UByte,
    val data: ByteArray,
) {
    companion object {
        fun parse(byteArray: ByteArray): SPIFlashRead? {
            if (byteArray.size < 5) return null

            val address = ByteBuffer.wrap(
                byteArray.toList().subList(0, 4).toByteArray()
            )
                .order(ByteOrder.LITTLE_ENDIAN)
                .int
                .toUInt()
            val length = byteArray[4].toUByte()
            val data = byteArray.toList().subList(5, byteArray.size).toByteArray()

            return SPIFlashRead(
                address, length, data
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SPIFlashRead

        if (address != other.address) return false
        if (length != other.length) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}