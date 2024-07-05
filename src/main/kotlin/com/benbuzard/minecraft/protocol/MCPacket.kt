package com.benbuzard.minecraft.protocol

import com.benbuzard.minecraft.protocol.utils.writeVarInt
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

//import okio.Buffer
//import okio.Sink
//import okio.buffer

interface MCPacket {
    val id: Int
//    val idBuffer: Buffer
    val idBuffer: MutableList<Byte>

    suspend fun writeData(sink: MutableList<Byte>)

    suspend fun write(sink: ByteWriteChannel) {
        val buffer = mutableListOf<Byte>()
        writeData(buffer)

//        sink.buffer().writeVarInt(buffer.size.toInt() + idBuffer.size.toInt())
//        sink.buffer().writeVarInt(id)
//        sink.buffer().writeAll(buffer)

//        sink.buffer().use {
            sink.writeVarInt(buffer.size.toInt() + idBuffer.size.toInt())
            sink.writeVarInt(id)
//            it.writeAll(buffer)
            sink.writeFully(buffer.toByteArray())
//        }
    }
}