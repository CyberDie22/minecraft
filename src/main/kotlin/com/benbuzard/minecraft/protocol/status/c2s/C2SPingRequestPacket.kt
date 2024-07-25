package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.C2SMCPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.protocol.status.s2c.S2CPongResponsePacket
import com.benbuzard.minecraft.protocol.utils.readLong
import com.benbuzard.minecraft.protocol.utils.writeLong
import com.benbuzard.minecraft.protocol.utils.writePacket
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source

@C2SPacketInfo(id = 0x01, state = ProtocolState.Status)
data class C2SPingRequestPacket(
    val payload: Long
) : C2SMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeLong(payload)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        val responsePacket = S2CPongResponsePacket(payload)
        sink.writePacket(responsePacket)

        player.socket.close()
    }

    companion object {
        fun read(source: Source, packetSize: Int): C2SMCPacket {
            val payload = source.readLong()

            return C2SPingRequestPacket(payload)
        }
    }
}