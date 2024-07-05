package com.benbuzard.minecraft.protocol.handshake.c2s

import com.benbuzard.minecraft.protocol.MCPacket
import com.benbuzard.minecraft.protocol.utils.*
import io.ktor.utils.io.core.*
import okio.Buffer
import okio.Sink
import okio.Source
import okio.buffer
import kotlin.properties.Delegates

data class C2SHandshakePacket(
    val protocolVersion: Int,
    val serverAddress: String,
    val serverPort: UShort,
    val nextState: Int
) : MCPacket {
    override val id = 0x00
    override val idBuffer: Buffer by lazy { Buffer().apply { writeVarInt(id) } }

    override suspend fun writeData(sink: Sink) {
        sink.buffer().use {
            it.writeVarInt(protocolVersion)
            it.writeMCString(serverAddress)
            it.writeUShort(serverPort)
            it.writeVarInt(nextState)
        }
    }

    companion object {
        fun read(source: Source): C2SHandshakePacket {
            var protocolVersion by Delegates.notNull<Int>()
            lateinit var serverAddress: String
            var serverPort by Delegates.notNull<UShort>()
            var nextState by Delegates.notNull<Int>()

            source.buffer().use {
                protocolVersion = it.readVarInt()
                serverAddress = it.readMCString()
                serverPort = it.readUShort()
                nextState = it.readVarInt()
            }

            return C2SHandshakePacket(protocolVersion, serverAddress, serverPort, nextState)
        }
    }
}