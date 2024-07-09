package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource

class C2SStatusRequestPacket : MCPacket {
    override val id: Int = 0x00
    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }

    override fun writeData(sink: RawSink) {
        // No data to write
    }

    companion object {
        fun read(source: RawSource): C2SStatusRequestPacket {
            return C2SStatusRequestPacket()
        }
    }
}