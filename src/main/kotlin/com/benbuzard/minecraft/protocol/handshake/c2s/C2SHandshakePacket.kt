package com.benbuzard.minecraft.protocol.handshake.c2s

import com.benbuzard.minecraft.protocol.C2SMCPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.protocol.utils.*
import com.benbuzard.minecraft.server.MinecraftServer
import com.benbuzard.minecraft.server.entities.ServerPlayer
import io.ktor.network.sockets.*
import okio.Sink
import okio.Source

@C2SPacketInfo(id = 0x00, state = ProtocolState.Handshake)
data class C2SHandshakePacket(
    val protocolVersion: Int,
    val serverAddress: String,
    val serverPort: UShort,
    val nextState: Int
) : C2SMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeVarInt(protocolVersion)
        sink.writeMCString(serverAddress)
        sink.writeUShort(serverPort)
        sink.writeVarInt(nextState)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        val logger = MinecraftServer.instance.logger

        player.protocolState = when (nextState) {
            1 -> ProtocolState.Status
            2 -> ProtocolState.Login
            3 -> ProtocolState.Transfer
            else -> throw IllegalStateException("Invalid next state $nextState")
        }

        logger.debug { "Switched protocol state: ${player.protocolState}" }
    }

    companion object {
        fun read(source: Source): C2SMCPacket {
            val protocolVersion = source.readVarInt()
            val serverAddress = source.readMCString()
            val serverPort = source.readUShort()
            val nextState = source.readVarInt()

            return C2SHandshakePacket(protocolVersion, serverAddress, serverPort, nextState)
        }
    }
}