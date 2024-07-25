package com.benbuzard.minecraft.protocol.login.c2s

import com.benbuzard.minecraft.protocol.C2SMCPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.protocol.login.s2c.S2CLoginSuccessPacket
import com.benbuzard.minecraft.protocol.utils.*
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source
import java.util.*

@C2SPacketInfo(id = 0x00, state = ProtocolState.Login)
data class C2SLoginStartPacket(
    val username: String,
    val uuid: UUID
) : C2SMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeMCString(username)
        sink.writeUUID(uuid)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        // TODO: encryption
        // TODO: compression

        val responsePacket = S2CLoginSuccessPacket(
            uuid = uuid,
            username = username,
            properties = emptyList(), // TODO: properties
            strictErrorHandling = true
        )
        sink.writePacket(responsePacket)
    }

    companion object {
        fun read(source: Source, packetSize: Int): C2SMCPacket {
            val username = source.readMCString()
            val uuid = source.readUUID()

            return C2SLoginStartPacket(username, uuid)
        }
    }
}