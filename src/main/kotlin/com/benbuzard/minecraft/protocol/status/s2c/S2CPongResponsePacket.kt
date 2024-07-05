package com.benbuzard.minecraft.protocol.status.s2c

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.writeLong
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import io.ktor.utils.io.*

//import okio.Buffer
//import okio.Sink
//import okio.Source
//import okio.buffer

data class S2CPongResponsePacket(val payload: Long) : MCPacket {
    override val id: Int = 0x01
//    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }
    override val idBuffer: MutableList<Byte> = mutableListOf<Byte>().apply { writeVarInt(id) }

    override suspend fun writeData(sink: MutableList<Byte>) {
        sink.writeLong(payload)
    }

    companion object {
        suspend fun read(source: ByteReadChannel): S2CPongResponsePacket {
            val payload = source.readLong()

            return S2CPongResponsePacket(payload)
        }
    }
}