package io.github.kaiseiyokoyama.joykonkt.controller.report.output

import kotlin.math.log2
import kotlin.math.roundToInt

class Rumble(
    freq: Double,
    amp: Double,
) {
    val frequency = if (freq < 0) {
        0.0
    } else if (freq > 1252.0) {
        1252.0
    } else {
        freq
    }

    val amplitude = if (amp < 0) {
        0.0
    } else if (amp > 1.02) {
        1.02
    } else {
        amp
    }

    fun encode(): ByteArray {
        val encodedHexFreq = (log2(frequency / 10.0) * 32.0).roundToInt().toUByte()

        val highFreq = if (encodedHexFreq > 0x60u) {
            encodedHexFreq - 0x60u
        } else {
            0u
        } * 4u
        val lowFreq = if (encodedHexFreq > 0x40u) {
            encodedHexFreq - 0x40u
        } else {
            0u
        }

        val encodedHexAmp = if (amplitude > 0.23) {
            (log2(amplitude * 8.7) * 32.0).roundToInt().toUByte()
        } else if (amplitude > 0.12) {
            (log2(amplitude * 17.0) * 16.0).roundToInt().toUByte()
        } else {
            0u // TODO: watch https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/rumble_data_table.md
        }

        val highFreqAmp = encodedHexAmp * 2u
        val lowFreqAmp = encodedHexAmp / 2u + 0x40u

        val array = ByteArray(4)
        array[0] = highFreq.and(0xFFu).toByte()
        array[1] = (highFreqAmp +
                // Add amp + 1st byte of frequency to amplitude byte
                highFreq.shr(8).and(0xFFu)).toByte()
        array[2] = (lowFreq +
                // Add freq + 1st byte of LF amplitude to the frequency byte
                lowFreqAmp.shr(8).and(0xFFu)).toByte()
        array[3] = lowFreqAmp.and(0xFFu).toByte()

        return array
    }
}