package io.github.kaiseiyokoyama.joykonkt.controller

enum class SubCommand(
    val byte: Byte
) {
    GetControllerState(0x00),
    /**
     * ## Arguments
     * - 0x30: Standard full mode
     * - 0x31: NFC/IR mode
     * - 0x3F: Simple HID mode
     */
    SetInputReportMode(0x03),
    EnableIMU(0x40),
    EnableVibration(0x48),
}