package com.benbuzard.minecraft.protocol.status.s2c

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.readMCString
import com.benbuzard.minecraft.protocol.utils.writeMCString
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import io.ktor.utils.io.*
import okio.Buffer
import okio.Sink
import okio.Source
import okio.buffer

data class S2CStatusResponsePacket(
    val jsonResponse: String
) : MCPacket {
    override val id: Int = 0x00
    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }

    override suspend fun writeData(sink: Sink) {
        sink.buffer().use {
            it.writeMCString(jsonResponse)
        }
    }

    companion object {
        fun read(source: Source): S2CStatusResponsePacket {
            lateinit var jsonResponse: String
            source.buffer().use {
                jsonResponse = it.readMCString()
            }

            return S2CStatusResponsePacket(jsonResponse)
        }
    }
}