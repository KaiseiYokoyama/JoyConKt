package io.github.kaiseiyokoyama.joykonkt.controller.report.input

enum class ID(
    val intValue: Int,
) {
    SubCommandReply(0x21),
    StandardFullMode(0x30),
    NFCIR(0x31),
    NormalReport(0x3F);

    companion object {
        fun fromByte(byte: Byte): ID? = ID.values()
            .filter { id -> id.intValue == byte.toInt() }
            .getOrNull(0)
    }
}

