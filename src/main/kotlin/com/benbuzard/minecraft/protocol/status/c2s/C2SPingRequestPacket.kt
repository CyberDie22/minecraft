package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.readLong
import com.benbuzard.minecraft.protocol.utils.writeLong
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource

data class C2SPingRequestPacket(val payload: Long) : MCPacket {
    override val id: Int = 0x01
    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }

    override fun writeData(sink: RawSink) {
        sink.writeLong(payload)
    }

    companion object {
        fun read(source: RawSource): C2SPingRequestPacket {
            val payload = source.readLong()

            return C2SPingRequestPacket(payload)
        }
    }
}