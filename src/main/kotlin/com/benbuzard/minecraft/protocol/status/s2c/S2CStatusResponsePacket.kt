package com.benbuzard.minecraft.protocol.status.s2c

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.readMCString
import com.benbuzard.minecraft.protocol.utils.writeMCString
import com.benbuzard.minecraft.protocol.utils.writeVarInt
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource

data class S2CStatusResponsePacket(
    val jsonResponse: String
) : MCPacket {
    override val id: Int = 0x00
    override val idBuffer: Buffer = Buffer().apply { writeVarInt(id) }

    override fun writeData(sink: RawSink) {
        sink.writeMCString(jsonResponse)
    }

    companion object {
        fun read(source: RawSource): S2CStatusResponsePacket {
            val jsonResponse = source.readMCString()

            return S2CStatusResponsePacket(jsonResponse)
        }
    }
}