package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import okio.Buffer
import okio.Sink
import okio.Source

class C2SStatusRequestPacket : MCPacket {
    override val id: Int = 0x00
    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }

    override suspend fun writeData(sink: Sink) {
        // No data to write
    }

    companion object {
        fun read(source: Source): C2SStatusRequestPacket {
            return C2SStatusRequestPacket()
        }
    }
}