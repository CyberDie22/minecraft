package com.benbuzard.minecraft.protocol.status.s2c

import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.protocol.S2CMCPacket
import com.benbuzard.minecraft.annotations.S2CPacketInfo
import com.benbuzard.minecraft.protocol.utils.readLong
import com.benbuzard.minecraft.protocol.utils.writeLong
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source

@S2CPacketInfo(id = 0x01, state = ProtocolState.Status)
data class S2CPongResponsePacket(
    val payload: Long
) : S2CMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeLong(payload)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        // TODO: client side
    }

    companion object {
        fun read(source: Source, packetSize: Int): S2CMCPacket {
            val payload = source.readLong()

            return S2CPongResponsePacket(payload)
        }
    }
}