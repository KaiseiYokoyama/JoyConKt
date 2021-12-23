package io.github.kaiseiyokoyama.joykonkt.controller

import org.hid4java.HidDevice

interface Controller {
    companion object {
        const val VENDOR: Int = 1406
    }

    fun setNonBlocking(nonBlocking: Boolean)

    fun read(): ByteArray

    fun read(data: ByteArray): Int

    fun read(amount: Int): ByteArray

    fun read(amount: Int, timeoutMillis: Int): ByteArray

    fun read(bytes: ByteArray, timeoutMillis: Int): Int

    fun getFeatureReport(data: ByteArray, reportId: Byte): Int

    fun sendFeatureReport(data: ByteArray, reportId: Byte): Int

    fun getIndexedString(index: Int): String

    fun write(message: ByteArray, packetLength: Int, reportId: Byte): Int

    fun devices(): Array<HidDevice>

    fun close()
}