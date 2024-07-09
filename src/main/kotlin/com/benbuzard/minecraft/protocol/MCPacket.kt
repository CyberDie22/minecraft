package com.benbuzard.minecraft.protocol

import com.benbuzard.minecraft.protocol.utils.writeVarInt
import kotlinx.io.Buffer
import kotlinx.io.RawSink

interface MCPacket {
    val id: Int
    val idBuffer: Buffer

    fun writeData(sink: RawSink)

    fun write(sink: RawSink) {
        val buffer = Buffer()
        writeData(buffer)

        sink.writeVarInt(buffer.size.toInt() + idBuffer.size.toInt())
        sink.writeVarInt(id)
        sink.write(buffer, buffer.size)
    }
}