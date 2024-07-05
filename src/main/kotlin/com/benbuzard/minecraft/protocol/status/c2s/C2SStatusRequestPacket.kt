package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import io.ktor.utils.io.*
//import okio.Buffer
//import okio.Sink
//import okio.Source

class C2SStatusRequestPacket : MCPacket {
    override val id: Int = 0x00
//    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }
    override val idBuffer: MutableList<Byte> = mutableListOf<Byte>().apply { writeVarInt(id) }

    override suspend fun writeData(sink: MutableList<Byte>) {
        // No data to write
    }

    companion object {
        fun read(source: ByteReadChannel): C2SStatusRequestPacket {
            return C2SStatusRequestPacket()
        }
    }
}