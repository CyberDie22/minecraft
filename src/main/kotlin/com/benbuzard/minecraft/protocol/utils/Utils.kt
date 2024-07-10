package com.benbuzard.minecraft.protocol.utils

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.ProtocolDirection
import com.benbuzard.minecraft.server.MinecraftServer
import com.benbuzard.minecraft.server.entities.ServerPlayer
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import okio.Source
import okio.Buffer
import okio.Sink
import okio.Timeout
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.functions
import kotlin.text.HexFormat
import kotlin.text.toByteArray

const val VARINT_SEGMENT_BITS = 0x7F.toByte()
const val VARINT_CONTINUE_BIT = 0x80.toByte()

fun ByteReadChannel.toSource() = object : Source {
    override fun close() {
        this@toSource.cancel()
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
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

    override fun timeout(): Timeout {
        return Timeout.NONE
    }
}

fun ByteWriteChannel.toSink() = object : Sink {
    override fun close() {
        this@toSink.close()
    }

    override fun flush() {
        this@toSink.flush()
    }

    override fun timeout(): Timeout {
        return Timeout.NONE
    }

    override fun write(source: Buffer, byteCount: Long) {
        // TODO: Try to remove copy, also get long instead of int
        val bytes = source.readByteArray(byteCount)
        runBlocking {
            this@toSink.writeFully(bytes)
        }
    }
}

// TODO: possibly dangerous if the map is not bijective
fun <K, V> Map<K, V>.reverse(): Map<V, K> = map { it.value to it.key }.toMap()


// TODO: this is realllllly dumb, make class that wraps the source/sink similar to RealSource/Sink instead of this mess

fun Source.readByte(): Byte {
    val buffer = Buffer()
    read(buffer, 1)
    return buffer.readByte()
}

fun Source.readBoolean(): Boolean = readByte() != 0.toByte()

fun Source.readShort(): Short {
    val buffer = Buffer()
    read(buffer, 2)
    return buffer.readShort()
}

fun Source.readUShort(): UShort = readShort().toUShort()

fun Source.readLong(): Long {
    val buffer = Buffer()
    read(buffer, 8)
    return buffer.readLong()
}

fun Source.readUTF8String(length: Long): String {
    val buffer = Buffer()
    read(buffer, length)
    return String(buffer.readByteArray())
}

fun Source.readMCString(): String {
    val length = readVarInt()
    return readUTF8String(length.toLong())
}

fun Source.readIdentifier(): Identifier {
    val combined = readMCString()
    val split = combined.split(":")
    val namespace = split[0]
    val value = split[1]
    return Identifier(namespace, value)
}

fun Source.readVarInt(): Int {
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

fun Source.readUUID(): UUID {
    val buffer = Buffer()
    read(buffer, 16)
    return UUID(buffer.readLong(), buffer.readLong())
}

@OptIn(ExperimentalStdlibApi::class)
fun Source.readPacket(player: ServerPlayer, direction: ProtocolDirection): MCPacket {
    val logger = MinecraftServer.instance.logger

    val packetSize = readVarInt()
    val packetId = readVarInt()
    logger.debug { "Read packet with id: 0x${packetId.toByte().toHexString(HexFormat.UpperCase)}" }

    val state = player.protocolState
    val packetClass = MinecraftServer.instance.packetRegistry.getPacket(packetId, state, direction)
    val companionInstance = packetClass?.companionObjectInstance
        ?: throw RuntimeException("No companion object found for packet ID: $packetId")
    val readMethod = packetClass.companionObject?.declaredMemberFunctions?.find { it.name == "read" }
        ?: throw RuntimeException("No read method found for packet ID: $packetId")
    val packet = readMethod.call(companionInstance, this) as MCPacket

    logger.debug { "Finished reading packet: $packet" }

    return packet
}



fun Sink.writeByte(value: Byte) {
    val buffer = Buffer()
    buffer.writeByte(value.toInt())
    write(buffer, 1)
}

fun Sink.writeBoolean(value: Boolean) = writeByte(if (value) 1 else 0)

fun Sink.writeShort(value: Short) {
    val buffer = Buffer()
    buffer.writeShort(value)
    write(buffer, 2)
}

fun Sink.writeUShort(value: UShort) = writeShort(value.toShort())

fun Sink.writeLong(value: Long) {
    val buffer = Buffer()
    buffer.writeLong(value)
    write(buffer, 8)
}

fun Sink.writeUTF8String(value: String) {
    val buffer = Buffer()
    buffer.write(value.toByteArray())
    write(buffer, buffer.size)
}

fun Sink.writeMCString(value: String) {
    writeVarInt(value.length)
    writeUTF8String(value)
}

fun Sink.writeIdentifier(value: Identifier) {
    writeMCString("${value.namespace}:${value.value}")
}

fun Sink.writeVarInt(value: Int) {
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

fun Sink.writeUUID(value: UUID) {
    val buffer = Buffer()
    buffer.writeLong(value.mostSignificantBits)
    buffer.writeLong(value.leastSignificantBits)
    write(buffer, 16)
}

fun Sink.writePacket(packet: MCPacket) {
    MinecraftServer.instance.logger.debug { "Writing packet: $packet" }

    val buffer = Buffer()
    buffer.writeVarInt(MinecraftServer.instance.packetRegistry.getId(packet::class)!!)
    packet.writeData(buffer)
    writeVarInt(buffer.size.toInt())
    write(buffer, buffer.size)
}