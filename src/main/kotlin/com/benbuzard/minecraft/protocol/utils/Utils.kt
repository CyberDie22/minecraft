package com.benbuzard.minecraft.protocol.utils

import com.benbuzard.minecraft.server.MinecraftServer
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.*
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

const val VARINT_SEGMENT_BITS = 0x7F.toByte()
const val VARINT_CONTINUE_BIT = 0x80.toByte()

fun ByteReadChannel.toSource() = object : RawSource {
    override fun close() {
        this@toSource.cancel()
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val bytes = runBlocking {
            // TODO: probably not the best way to do this
            val buffer = ByteBuffer.allocate(byteCount.toInt())
            this@toSource.readAvailable(buffer)
            buffer.flip()
            buffer
        }
        sink.write(bytes)
        return bytes.limit().toLong()
    }
}

fun ByteWriteChannel.toSink() = object : RawSink {
    override fun close() {
        this@toSink.close()
    }

    override fun flush() {
        this@toSink.flush()
    }

    override fun write(source: Buffer, byteCount: Long) {
        // TODO: Try to remove copy, also get long instead of int
        val bytes = source.readByteArray(byteCount.toInt())
        runBlocking {
            this@toSink.writeFully(bytes)
        }
    }
}


// TODO: this is realllllly dumb, make class that wraps the source/sink similar to RealSource/Sink instead of this mess

fun RawSource.readByte(): Byte {
    val buffer = Buffer()
    readAtMostTo(buffer, 1)
    return buffer.readByte()
}

fun RawSource.readShort(): Short {
    val buffer = Buffer()
    readAtMostTo(buffer, 2)
    return buffer.readShort()
}

fun RawSource.readUShort(): UShort = readShort().toUShort()

fun RawSource.readLong(): Long {
    val buffer = Buffer()
    readAtMostTo(buffer, 8)
    return buffer.readLong()
}

fun RawSource.readUTF8String(length: Long): String {
    val buffer = Buffer()
    readAtMostTo(buffer, length)
    return String(buffer.readByteArray())
}

fun RawSource.readMCString(): String {
    val length = readVarInt()
    return readUTF8String(length.toLong())
}

fun RawSource.readVarInt(): Int {
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



fun RawSink.writeByte(value: Byte) {
    val buffer = Buffer()
    buffer.writeByte(value)
    write(buffer, 1)
}

fun RawSink.writeShort(value: Short) {
    val buffer = Buffer()
    buffer.writeShort(value)
    write(buffer, 2)
}

fun RawSink.writeUShort(value: UShort) = writeShort(value.toShort())

fun RawSink.writeLong(value: Long) {
    val buffer = Buffer()
    buffer.writeLong(value)
    write(buffer, 8)
}

fun RawSink.writeUTF8String(value: String) {
    val buffer = Buffer()
    buffer.write(value.toByteArray())
    write(buffer, buffer.size)
}

fun RawSink.writeMCString(value: String) {
    writeVarInt(value.length)
    writeUTF8String(value)
}

fun RawSink.writeVarInt(value: Int) {
    var mutableValue = value
    while (true) {
        if ((mutableValue and VARINT_SEGMENT_BITS.inv().toInt()) == 0) {
            writeByte(mutableValue.toByte())
            return
        }

        writeByte((mutableValue.toByte() and VARINT_SEGMENT_BITS) or VARINT_CONTINUE_BIT)

        // Note: ushr is the unsigned right shift operator in Kotlin
        mutableValue = mutableValue ushr 7
    }
}