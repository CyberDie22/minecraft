package com.benbuzard.minecraft.protocol.status.s2c

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.readMCString
import com.benbuzard.minecraft.protocol.utils.writeMCString
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

//import okio.Buffer
//import okio.Sink
//import okio.Source
//import okio.buffer

data class S2CStatusResponsePacket(
    val jsonResponse: String
) : MCPacket {
    override val id: Int = 0x00
//    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }\
    override val idBuffer: MutableList<Byte> = mutableListOf<Byte>().apply { writeVarInt(id) }

    override suspend fun writeData(sink: MutableList<Byte>) {
//        sink.buffer().use {
            sink.writeMCString(jsonResponse)
//        }
    }

    companion object {
        suspend fun read(source: ByteReadChannel): S2CStatusResponsePacket {
            lateinit var jsonResponse: String
//            source.buffer().use {
                jsonResponse = source.readMCString()
//            }

            return S2CStatusResponsePacket(jsonResponse)
        }
    }
}