package io.github.kaiseiyokoyama.joykonkt.controller.report.output

enum class ID(
    val intValue: Int,
) {
    RumbleSubCommand(0x01),
    Rumble(0x10);

    companion object {
        fun fromByte(byte: Byte): io.github.kaiseiyokoyama.joykonkt.controller.report.input.ID? = io.github.kaiseiyokoyama.joykonkt.controller.report.input.ID.values()
            .filter { id -> id.intValue == byte.toInt() }
            .getOrNull(0)
    }
}