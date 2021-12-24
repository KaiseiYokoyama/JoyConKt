package io.github.kaiseiyokoyama.joykonkt.controller.report.output

enum class PlayerLight(
    val byte: UByte
) {
    Off(0x00u), On(0x01u), Flash(0x10u);

    companion object {
        fun decode(
            led0: PlayerLight, // Closest led to SL Button
            led1: PlayerLight,
            led2: PlayerLight,
            led3: PlayerLight, // Closest led to SR Button
        ): UByte =
            0x00u
                .or(
                    led0.byte * 1u
                )
                .or(
                    led1.byte * 2u
                )
                .or(
                    led2.byte * 4u
                )
                .or(
                    led3.byte * 8u
                )
                .toUByte()
    }
}