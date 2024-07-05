package com.benbuzard.minecraft.protocol

import com.benbuzard.minecraft.protocol.utils.writeVarInt
import okio.Buffer
import okio.Sink
import okio.buffer

interface MCPacket {
    val id: Int
    val idBuffer: Buffer

    suspend fun writeData(sink: Sink)

    suspend fun write(sink: Sink) {
        val buffer = Buffer()
        writeData(buffer)

//        sink.buffer().writeVarInt(buffer.size.toInt() + idBuffer.size.toInt())
//        sink.buffer().writeVarInt(id)
//        sink.buffer().writeAll(buffer)

        sink.buffer().use {
            println("size")
            it.writeVarInt(buffer.size.toInt() + idBuffer.size.toInt())
            it.flush()
            println("id")
            it.writeVarInt(id)
            it.flush()
            println("buffer")
            it.writeAll(buffer)
            it.flush()
            println("done")
        }
    }
}