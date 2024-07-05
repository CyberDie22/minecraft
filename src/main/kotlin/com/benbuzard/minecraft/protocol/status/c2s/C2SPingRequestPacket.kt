package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import okio.Buffer
import okio.Sink
import okio.Source
import okio.buffer

data class C2SPingRequestPacket(val payload: Long) : MCPacket {
    override val id: Int = 0x01
    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }

    override suspend fun writeData(sink: Sink) {
        sink.buffer().writeLong(payload)
    }

    companion object {
        fun read(source: Source): C2SPingRequestPacket {
            val payload = source.buffer().readLong()

            return C2SPingRequestPacket(payload)
        }
    }
}