package io.github.kaiseiyokoyama.joykonkt.controller.report.input.calibration

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

data class Sticks(
    val left: Stick,
    val right: Stick,
) {
    companion object {
        fun parse(byteArray: ByteArray): Sticks? {
            if (byteArray.size < 18) return null

            val left = Stick.parse(
                byteArray.toList().subList(0, 9).toByteArray()
            ) ?: return null
            val right = Stick.parse(
                byteArray.toList().subList(9, 18).toByteArray()
            ) ?: return null

            val lStick = Stick(
                horizontal = Stick.Axis.new(left[2], left[0], left[4]),
                vertical = Stick.Axis.new(left[3], left[1], left[5]),
            )
            val rStick = Stick(
                horizontal = Stick.Axis.new(right[0], right[4], right[2]),
                vertical = Stick.Axis.new(right[1], right[5], right[3]),
            )

            return Sticks(lStick, rStick)
        }
    }

    data class Stick(
        val horizontal: Axis,
        val vertical: Axis,
    ) {
        companion object {
            fun parse(byteArray: ByteArray): List<UShort>? {
                if (byteArray.size < 9) return null

                val data = mutableListOf<UShort>(
                    0u, 0u, 0u, 0u, 0u, 0u
                )
                // byteArray[0] | (byteArray[1] << 8) & 0xF00
                data[0] = ByteBuffer.wrap(
                    arrayOf(byteArray[0], byteArray[1].and(0xF)).toByteArray()
                )
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .short
                    .toUShort()
                // byteArray[1] >> 4 | byteArray[2] << 4
                data[1] = (byteArray[1].toUByte() / 0x10u + byteArray[2].toUByte() * 0x10u).toUShort()
                data[2] = ByteBuffer.wrap(
                    arrayOf(byteArray[3], byteArray[4].and(0xF)).toByteArray()
                )
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .short
                    .toUShort()
                data[3] = (byteArray[4].toUByte() / 0x10u + byteArray[5].toUByte() * 0x10u).toUShort()
                data[4] = ByteBuffer.wrap(
                    arrayOf(byteArray[6], byteArray[7].and(0xF)).toByteArray()
                )
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .short
                    .toUShort()
                data[5] = (byteArray[7].toUByte() / 0x10u + byteArray[8].toUByte() * 0x10u).toUShort()

                return data
            }
        }

        data class Axis(
            val max: UShort,
            val center: UShort,
            val min: UShort,
        ) {
            companion object {
                fun new(
                    center: UShort,
                    above: UShort,
                    below: UShort,
                ): Axis {
                    return Axis((center + above).toUShort(), center, (center - below).toUShort())
                }
            }
        }
    }
}