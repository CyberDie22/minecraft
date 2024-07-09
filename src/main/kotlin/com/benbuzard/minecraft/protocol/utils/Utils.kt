package com.benbuzard.minecraft.protocol.utils

import com.benbuzard.minecraft.server.MinecraftServer
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.*
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.inv

const val VARINT_SEGMENT_BITS = 0x7F.toByte()
const val VARINT_CONTINUE_BIT = 0x80.toByte()



suspend fun ByteReadChannel.readUByte(): UByte = readByte().toUByte()

suspend fun ByteReadChannel.readUShort(): UShort = readShort().toUShort()

suspend fun ByteReadChannel.readMCString(): String {
    val length = readVarInt()
    val bytes = ByteArray(length)
    readFully(bytes)
    return String(bytes)
}

suspend fun ByteReadChannel.readVarInt(): Int {
    var value = 0
    var position = 0
    var currentByte: Byte

    while (true) {
        currentByte = readByte()
        value = value or ((currentByte and VARINT_SEGMENT_BITS).toInt() shl position)

        if ((currentByte and VARINT_CONTINUE_BIT).toInt() == 0) break

        position += 7

        if (position >= 32) throw RuntimeException("VarInt is too big")
    }

    return value
}

suspend fun ByteWriteChannel.writeBoolean(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeUByte(value: UByte) = writeByte(value.toByte().toInt())

suspend fun ByteWriteChannel.writeUShort(value: UShort) = writeShort(value.toInt())

suspend fun ByteWriteChannel.writeMCString(value: String) {
    val bytes = value.toByteArray()
    writeVarInt(bytes.size)
    writeAvailable(bytes)
}

suspend fun ByteWriteChannel.writeVarInt(value: Int) {
    var mutableValue = value
    while (true) {
        if ((mutableValue and VARINT_SEGMENT_BITS.inv().toInt()) == 0) {
            writeByte(mutableValue)
            return
        }

        writeByte((mutableValue and VARINT_SEGMENT_BITS.toInt()) or VARINT_CONTINUE_BIT.toInt())

        // Note: ushr is the unsigned right shift operator in Kotlin
        mutableValue = mutableValue ushr 7
    }
}

fun MutableList<Byte>.writeUShort(value: UShort) {
    add((value.toInt() ushr 8).toByte())
    add(value.toByte())
}

fun MutableList<Byte>.writeLong(value: Long) {
    add((value ushr 56).toByte())
    add((value ushr 48).toByte())
    add((value ushr 40).toByte())
    add((value ushr 32).toByte())
    add((value ushr 24).toByte())
    add((value ushr 16).toByte())
    add((value ushr 8).toByte())
    add(value.toByte())
}

fun MutableList<Byte>.writeMCString(value: String) {
    val bytes = value.toByteArray()
    writeVarInt(bytes.size)
    addAll(bytes.toList())

}

fun MutableList<Byte>.writeVarInt(value: Int) {
    var mutableValue = value
    while (true) {
        if ((mutableValue and VARINT_SEGMENT_BITS.inv().toInt()) == 0) {
            add(mutableValue.toByte())
            return
        }

        add((mutableValue and VARINT_SEGMENT_BITS.toInt() or VARINT_CONTINUE_BIT.toInt()).toByte())

        // Note: ushr is the unsigned right shift operator in Kotlin
        mutableValue = mutableValue ushr 7
    }
}