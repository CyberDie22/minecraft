package com.benbuzard.minecraft.protocol.login.c2s

import com.benbuzard.minecraft.protocol.C2SMCPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.protocol.login.s2c.S2CFeatureFlagsPacket
import com.benbuzard.minecraft.protocol.utils.Identifier
import com.benbuzard.minecraft.protocol.utils.writePacket
import com.benbuzard.minecraft.server.MinecraftServer
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source

@C2SPacketInfo(id = 0x03, state = ProtocolState.Login)
class C2SLoginAcknowledgedPacket : C2SMCPacket {
    override fun writeData(sink: Sink) {
        // No data to write
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        val logger = MinecraftServer.instance.logger

        val responsePacket = S2CFeatureFlagsPacket(listOf(
            Identifier("minecraft", "vanilla")
        ))
        sink.writePacket(responsePacket)
    }

    companion object {
        fun read(source: Source): C2SMCPacket {
            return C2SLoginAcknowledgedPacket()
        }
    }
}