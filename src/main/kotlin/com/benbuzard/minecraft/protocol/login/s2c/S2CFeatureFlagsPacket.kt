package com.benbuzard.minecraft.protocol.login.s2c

import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.protocol.S2CMCPacket
import com.benbuzard.minecraft.annotations.S2CPacketInfo
import com.benbuzard.minecraft.protocol.utils.*
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source

@S2CPacketInfo(id = 0x02, state = ProtocolState.Login)
data class S2CFeatureFlagsPacket(
    val features: List<Identifier>,
) : S2CMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeVarInt(features.size)
        features.forEach {
            sink.writeIdentifier(it)
        }
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        // TODO: client side
    }

    companion object {
        fun read(source: Source): S2CMCPacket {
            val features = (0 until source.readVarInt()).map {
                source.readIdentifier()
            }

            return S2CFeatureFlagsPacket(features)
        }
    }
}