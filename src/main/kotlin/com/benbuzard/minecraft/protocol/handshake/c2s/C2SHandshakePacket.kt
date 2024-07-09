package com.benbuzard.minecraft.protocol.handshake.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.*
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource

data class C2SHandshakePacket(
    val protocolVersion: Int,
    val serverAddress: String,
    val serverPort: UShort,
    val nextState: Int
) : MCPacket {
    override val id = 0x00
    override val idBuffer: Buffer by lazy { Buffer().apply { writeVarInt(id) } }

    override fun writeData(sink: RawSink) {
        sink.writeVarInt(protocolVersion)
        sink.writeMCString(serverAddress)
        sink.writeUShort(serverPort)
        sink.writeVarInt(nextState)
    }

    companion object {
        fun read(source: RawSource): C2SHandshakePacket {
            val protocolVersion = source.readVarInt()
            val serverAddress = source.readMCString()
            val serverPort = source.readUShort()
            val nextState = source.readVarInt()

            return C2SHandshakePacket(protocolVersion, serverAddress, serverPort, nextState)
        }
    }
}