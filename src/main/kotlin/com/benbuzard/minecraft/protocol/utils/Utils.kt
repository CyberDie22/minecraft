package com.benbuzard.minecraft.protocol.utils

import com.benbuzard.minecraft.server.MinecraftServer
import okio.*
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

const val VARINT_SEGMENT_BITS = 0x7F.toByte()
const val VARINT_CONTINUE_BIT = 0x80.toByte()

fun BufferedSource.readBoolean(): Boolean = readByte() != 0.toByte()

fun BufferedSource.readUByte(): UByte = readByte().toUByte()

fun BufferedSource.readUShort(): UShort = readShort().toUShort()

fun BufferedSource.readFloat(): Float = Float.fromBits(readInt())

fun BufferedSource.readDouble(): Double = Double.fromBits(readLong())

fun BufferedSource.readMCString(): String {
    val length = readVarInt()
    val bytes = ByteArray(length)
    readFully(bytes)
    return String(bytes)
}

fun BufferedSource.readVarInt(): Int {
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

fun BufferedSink.writeBoolean(value: Boolean) = writeByte(if (value) 1 else 0)

fun BufferedSink.writeUByte(value: UByte) = writeByte(value.toByte().toInt())

fun BufferedSink.writeUShort(value: UShort) = writeShort(value.toInt())

fun BufferedSink.writeFloat(value: Float) = writeInt(value.toBits())

fun BufferedSink.writeDouble(value: Double) = writeLong(value.toBits())

fun BufferedSink.writeMCString(value: String) {
    val bytes = value.toByteArray()
    writeVarInt(bytes.size)
    write(bytes)
}

fun BufferedSink.writeVarInt(value: Int) {
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

//suspend fun ByteReadChannel.readFully(buffer: ByteArray) {
//    var position = 0
//
//    while (position < buffer.size) {
//        val read = readAvailable(buffer, position, buffer.size - position)
//        if (read == -1) throw EOFException("Unexpected end of stream")
//        position += read
//    }
//}
//
//suspend fun ByteReadChannel.readVarInt(): Int {
//    return readVarInt(this::readByte)
//}
//
//suspend fun ByteArrayInputStream.readVarInt(): Int {
//    return readVarInt { read().toByte() }
//}
//
//suspend fun MutableList<Byte>.readVarInt(): Int {
//    return readVarInt { removeFirst() }
//}
//
//suspend fun ByteWriteChannel.writeVarInt(value: Int) {
//    writeVarInt(value, this::writeByte)
//}
//
//suspend fun MutableList<Byte>.writeVarInt(value: Int) {
//    writeVarInt(value) { add(it) }
//}
//
//fun ByteArrayInputStream.readMCString(): String {
//    val length = readVarInt()
//    val bytes = ByteArray(length)
//    read(bytes, 0, length)
//    return String(bytes)
//}
//
//suspend fun ByteWriteChannel.writeMCString(value: String) {
//    val bytes = value.toByteArray()
//    writeVarInt(bytes.size)
//    writeFully(bytes)
//}
//
//fun ByteArrayInputStream.readUShort(): UShort {
//    val low = read()
//    val high = read()
//    return ((low shl 8) or high).toUShort()
//}
//
//suspend fun ByteWriteChannel.writeUShort(value: UShort) {
//    writeByte((value.toInt() shr 8).toByte())
//    writeByte(value.toByte())
//}