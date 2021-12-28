package io.github.kaiseiyokoyama.joykonkt.controller.report.input

import io.github.kaiseiyokoyama.joykonkt.controller.SubCommand
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

sealed class InputReport(
    open val inputReportId: ID
)

data class NormalReport(
    val pressedButtons: Set<Button>,
    val stickDirection: StickDirection,
//    val sticks: Sticks,
) : InputReport(ID.NormalReport) {
    companion object {
        fun parse(data: ByteArray): Result<NormalReport> {
            // バイト列の長さが足りない
            if (data.size < 12) {
                return Result.failure(
                    TooShortByteArray(
                        data, ID.NormalReport, data.size, 12
                    )
                )
            }

            val id = ID.fromByte(data[0])
            // input report idが違う
            if (id != ID.NormalReport) {
                return Result.failure(
                    WrongInputReportID(
                        data, ID.NormalReport, id, data[0]
                    )
                )
            }

            // 押されているボタンのセット
            val pressedButtons = Button.pressedButtons(data.toList().subList(1, 3))

            // スティックの向き
            val stickDirection = StickDirection.direction(data[3]) ?: return Result.failure(
                ParseError(data, ID.NormalReport, "Failed to parse data[3] = ${data[3]} to stick direction.")
            )

            // スティックの傾き
            val sticks = Sticks.parse(data.toList().subList(4, 12)) ?: return Result.failure(
                ParseError(
                    data,
                    ID.NormalReport,
                    "Failed to parse data.toList().subList(4, 12) = ${
                        data.toList().subList(4, 12)
                    } to stick directions."
                )
            )

            return Result.success(
                NormalReport(pressedButtons, stickDirection)
            )
        }
    }

    enum class Button(
        val bit: UByte,
        val index: Int,
    ) {
        // -- first byte --
        Down(0x01u, 0), Right(0x02u, 0), Left(0x04u, 0), Up(0x08u, 0),
        SL(0x10u, 0), SR(0x20u, 0),

        // -- second byte --
        Minus(0x01u, 1), Plus(0x02u, 1), LeftStick(0x04u, 1), RightStick(0x08u, 1),
        Home(0x10u, 1), Capture(0x20u, 1), LR(0x40u, 1), ZLZR(0x80u, 1);

        private fun isPressed(x: List<Byte>): Boolean {
            return if (x.size < 2) {
                false
            } else {
                x[index].toUByte().and(bit) == bit
            }
        }

        companion object {
            internal fun pressedButtons(x: List<Byte>) = values()
                .filter { button ->
                    button.isPressed(x)
                }
                .toSet()
        }
    }

    /**
     * Stick direction based on landscape.
     *
     * | SL | SYNC | SR |
     * |---:|:---:|:---|
     * | 7  |  0   |  1 |
     * | 6  |  8   |  2 |
     * | 5  |  4   |  3 |
     */
    enum class StickDirection(
        val byte: UByte
    ) {
        UpperLeft(0x07u), Up(0x00u), UpperRight(0x01u),
        Left(0x07u), Neutral(0x08u), Right(0x02u),
        DownerLeft(0x05u), Down(0x04u), DownerRight(0x03u);

        companion object {
            internal fun direction(byte: Byte): StickDirection? = values()
                .filter { d -> d.byte == byte.toUByte() }
                .getOrNull(0)
        }
    }

    data class Sticks(
        val LStick: Stick,
        val RStick: Stick,
    ) {
        companion object {
            // it does not work correctly
            fun parse(byteArray: List<Byte>): Sticks? {
                fun decode(byteArray: List<Byte>): Stick {
                    val horizontal = ByteBuffer.wrap(
//                        byteArray.subList(0,2).toByteArray()
                        arrayOf(byteArray[0], byteArray[1]).toByteArray()
                    )
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .short
                        .toUInt()

                    val vertical = ByteBuffer.wrap(
                        byteArray.subList(2, 4).toByteArray()
                    )
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .short
                        .toUInt()

                    return Stick(horizontal, vertical)
                }

                if (byteArray.size < 8) return null

                return Sticks(
                    LStick = decode(byteArray.subList(0, 4)),
                    RStick = decode(byteArray.subList(4, 8)),
                )
            }
        }
    }
}

data class StandardFullReport private constructor(
    val timer: UByte,
    val battery: Battery,
    val connection: Connection,
    val buttons: Buttons,
    val sticks: Sticks,
    val vibratorInputReport: Byte,
    val payLoad: PayLoad
) : InputReport(payLoad.id) {
    companion object {
        fun parse(data: ByteArray): Result<StandardFullReport> {
            // input report idが違う
            val id = ID.fromByte(data[0])
            when (id) {
                ID.SubCommandReply, ID.StandardFullMode, ID.NFCIR -> {}
                else -> return Result.failure(
                    WrongInputReportID(
                        data, ID.StandardFullMode, id, data[0]
                    )
                )
            }

            // バイト列の長さが足りない
            val minLength = when (id) {
                ID.SubCommandReply -> 13
                ID.StandardFullMode -> 49
                ID.NFCIR -> 13
                else -> throw IllegalStateException("Unreachable Arm")
            }
            if (data.size < minLength) {
                return Result.failure(
                    TooShortByteArray(
                        data, id, data.size, minLength
                    )
                )
            }

            val timer = data[1].toUByte()
            val battery = data[2].toInt().shr(4).and(0x0F).toUByte().let {
                Battery.parse(it) ?: return Result.failure(
                    ParseError(
                        data,
                        id,
                        "Failed to parse 2 high nibble of data[2] = ${it} to battery level"
                    )
                )
            }
            val connection = data[2].toInt().and(0x0F).toUByte().let {
                Connection.parse(it) ?: return Result.failure(
                    ParseError(data, id, "Failed to parse 2 low nibble of data[2] = ${it} to connection info")
                )
            }
            val buttons = Buttons.parse(data.toList().subList(3, 6).toByteArray()) ?: return Result.failure(
                ParseError(
                    data, id, "Failed to parse data[3..6] = ${
                        data.toList().subList(3, 6).toByteArray().contentToString()
                    } to button status"
                )
            )
            val sticks = Sticks.parse(data.toList().subList(6, 12).toByteArray()) ?: return Result.failure(
                ParseError(
                    data, id, "Failed to parse data[6..12] = ${
                        data.toList().subList(6, 12).toByteArray().contentToString()
                    } to stick states"
                )
            )
            val vibratorInputReport = data[12]

            val payload = when (id) {
                ID.SubCommandReply -> PayLoad.SubCommandReply.parse(
                    data.toList().subList(13, minLength.coerceAtLeast(data.size)).toByteArray()
                )
                ID.NFCIR -> PayLoad.NFCIR(
                    data.toList().subList(13, minLength.coerceAtLeast(data.size)).toByteArray()
                )
                ID.StandardFullMode -> PayLoad.IMUData3Frames.parse(
                    data.toList().subList(13, minLength).toByteArray()
                )
                else -> throw IllegalStateException("Unreachable Arm")
            } ?: return Result.failure(
                ParseError(
                    data, id, "Failed to parse data[13..$minLength] ${
                        data.toList().subList(13, minLength).toByteArray().contentToString()
                    } to payload"
                )
            )

            return Result.success(
                StandardFullReport(
                    timer, battery, connection, buttons, sticks, vibratorInputReport, payload
                )
            )

        }
    }

    sealed class PayLoad(
        val id: ID
    ) {
        class SubCommandReply private constructor(
            val ack: Byte,
            val subCommand: SubCommand?,
            val subCommandUByte: UByte,
            val data: ByteArray,
        ) : PayLoad(ID.SubCommandReply) {
            companion object {
                fun parse(byteArray: ByteArray): SubCommandReply? {
                    if (byteArray.size < 2) return null

                    val ack = byteArray[0]
                    val subCommand = SubCommand.fromByte(byteArray[1])
                    val subCommandUByte = byteArray[1].toUByte()
                    val data = byteArray.toList()
                        .subList(2, byteArray.size)
                        .toByteArray()

                    return SubCommandReply(
                        ack, subCommand, subCommandUByte, data
                    )
                }
            }
        }

        data class IMUData3Frames private constructor(
            val imudata: Array<IMUData>
        ) : PayLoad(ID.StandardFullMode) {
            companion object {
                fun parse(byteArray: ByteArray): IMUData3Frames? {
                    if (byteArray.size < 36) return null

                    val imuData = arrayOf(
                        IMUData.parse(byteArray.toList().subList(0, 12).toByteArray()) ?: return null,
                        IMUData.parse(byteArray.toList().subList(12, 24).toByteArray()) ?: return null,
                        IMUData.parse(byteArray.toList().subList(24, 36).toByteArray()) ?: return null,
                    )

                    return IMUData3Frames(imuData)
                }
            }

            data class IMUData(
                val acc: Acc,
                val gyro: Gyro,
            ) {
                companion object {
                    fun parse(byteArray: ByteArray): IMUData? {
                        if (byteArray.size < 12) return null

                        val acc = Acc.parse(byteArray.toList().subList(0, 6).toByteArray()) ?: return null
                        val gyro = Gyro.parse(byteArray.toList().subList(6, 12).toByteArray()) ?: return null

                        return IMUData(acc, gyro)
                    }
                }

                data class Acc(
                    val x: Short,
                    val y: Short,
                    val z: Short,
                ) {
                    companion object {
                        fun parse(byteArray: ByteArray): Acc? {
                            fun decode(byteArray: List<Byte>): Short = ByteBuffer.wrap(byteArray.toByteArray())
                                .order(ByteOrder.LITTLE_ENDIAN).short

                            if (byteArray.size < 6) return null

                            val byteArray = byteArray.toList()

                            val x = decode(byteArray.subList(0, 2))
                            val y = decode(byteArray.subList(2, 4))
                            val z = decode(byteArray.subList(4, 6))

                            return Acc(x, y, z)
                        }
                    }
                }

                data class Gyro(
                    val xAxis: Short,
                    val yAxis: Short,
                    val zAxis: Short,
                ) {
                    companion object {
                        fun parse(byteArray: ByteArray): Gyro? {
                            fun decode(byteArray: List<Byte>): Short = ByteBuffer.wrap(byteArray.toByteArray())
                                .order(ByteOrder.LITTLE_ENDIAN).short

                            if (byteArray.size < 6) return null

                            val byteArray = byteArray.toList()

                            val xAxis = decode(byteArray.subList(0, 2))
                            val yAxis = decode(byteArray.subList(2, 4))
                            val zAxis = decode(byteArray.subList(4, 6))

                            return Gyro(xAxis, yAxis, zAxis)
                        }
                    }
                }
            }
        }

        data class NFCIR(
            val data: ByteArray,
        ) : PayLoad(ID.NFCIR)
    }

    data class Battery(
        val level: Level,
        val charging: Boolean,
    ) {
        companion object {
            fun parse(byte: UByte): Battery? {
                val level = Level.parse(byte) ?: return null
                val charging = byte.toInt() % 2 == 1

                return Battery(level, charging)
            }
        }

        enum class Level(
            val byte: UByte
        ) {
            Full(4u), Medium(3u), Low(2u), Critical(1u), Empty(0u);

            companion object {
                fun parse(byte: UByte) = values()
                    .filter { v -> v.byte == (byte.toInt() / 2).toUByte() }
                    .getOrNull(0)
            }
        }
    }

    data class Connection(
        val kind: Kind,
        val powered: Boolean,
    ) {
        companion object {
            fun parse(byte: UByte): Connection? {
                val kind = Kind.parse(byte) ?: return null
                val powered = byte.toUInt().and(1u) == 1u

                return Connection(kind, powered)
            }
        }

        enum class Kind(
            val int: UInt
        ) {
            JoyCon(3u),
            ProConOrChrGrip(0u);

            companion object {
                fun parse(byte: UByte): Kind? = values()
                    .filter { v -> v.int == byte.toUInt().shr(1).and(3u) }
                    .getOrNull(0)
            }
        }
    }

    data class Buttons(
        val pressedButtons: Set<Button>
    ) {
        companion object {
            fun parse(byteArray: ByteArray): Buttons? {
                if (byteArray.size < 3) return null

                val pressedButtons = Button.values()
                    .filter { b ->
                        byteArray[b.index].toInt().and(b.uByte.toInt()) == b.uByte.toInt()
                    }
                    .toSet()

                return Buttons(pressedButtons)
            }
        }

        enum class Button(
            val uByte: UByte,
            val index: Int,
        ) {
            // -- first byte --
            Y(0x01u, 0),
            X(0x02u, 0),
            B(0x04u, 0),
            A(0x08u, 0),
            SR_Right(0x10u, 0),
            SL_Right(0x20u, 0),
            R(0x40u, 0),
            ZR(0x80u, 0),

            // -- second byte --
            Minus(0x01u, 1),
            Plus(0x02u, 1),
            RStick(0x04u, 1),
            LStick(0x08u, 1),
            Home(0x10u, 1),
            Capture(0x20u, 1),

            //            Nothing(0x40u, 0),
            ChargingGrip(0x80u, 1),

            // -- third byte --
            Down(0x01u, 2),
            Up(0x02u, 2),
            Right(0x04u, 2),
            Left(0x08u, 2),
            SR_Left(0x10u, 2),
            SL_Left(0x20u, 2),
            L(0x40u, 2),
            ZL(0x80u, 2);
        }
    }

    data class Sticks(
        val LStick: Stick,
        val RStick: Stick,
    ) {
        companion object {
            fun parse(byteArray: ByteArray): Sticks? {
                fun decode(byteArray: ByteArray): Stick {
                    val horizontal = ByteBuffer.wrap(
                        arrayOf(byteArray[0], byteArray[1].and(0xF)).toByteArray()
                    )
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .short
                        .toUInt()

                    val vertical = byteArray[1].toUByte() / 0x10u + byteArray[2].toUByte() * 0x10u

                    return Stick(horizontal, vertical)
                }

                if (byteArray.size < 6) return null

                return Sticks(
                    LStick = decode(byteArray.toList().subList(0, 3).toByteArray()),
                    RStick = decode(byteArray.toList().subList(3, 6).toByteArray()),
                )
            }
        }
    }
}

data class Stick(
    val horizontal: UInt,
    val vertical: UInt,
)

sealed class Error(
    open val byteArray: ByteArray,
    open val reportID: ID,
) : Throwable()

data class WrongInputReportID(
    override val byteArray: ByteArray,
    override val reportID: ID,
    val currentID: ID?,
    val currentIDInt: Byte,
) : Error(byteArray, reportID)

data class TooShortByteArray(
    override val byteArray: ByteArray,
    override val reportID: ID,
    val currentLength: Int,
    val requiredLength: Int,
) : Error(byteArray, reportID)

data class ParseError(
    override val byteArray: ByteArray,
    override val reportID: ID,
    override val message: String,
) : Error(byteArray, reportID)