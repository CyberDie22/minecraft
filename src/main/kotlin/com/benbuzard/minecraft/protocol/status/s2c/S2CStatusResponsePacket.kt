package com.benbuzard.minecraft.protocol.status.s2c

import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.protocol.S2CMCPacket
import com.benbuzard.minecraft.annotations.S2CPacketInfo
import com.benbuzard.minecraft.protocol.utils.readMCString
import com.benbuzard.minecraft.protocol.utils.writeMCString
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source

@S2CPacketInfo(id = 0x00, state = ProtocolState.Status)
data class S2CStatusResponsePacket(
    val jsonResponse: String
) : S2CMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeMCString(jsonResponse)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        // TODO: client side
    }

    companion object {
        fun read(source: Source, packetSize: Int): S2CMCPacket {
            val jsonResponse = source.readMCString()

            return S2CStatusResponsePacket(jsonResponse)
        }
    }
}