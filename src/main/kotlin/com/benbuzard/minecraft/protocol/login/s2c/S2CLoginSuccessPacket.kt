package com.benbuzard.minecraft.protocol.login.s2c

import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.protocol.S2CMCPacket
import com.benbuzard.minecraft.annotations.S2CPacketInfo
import com.benbuzard.minecraft.protocol.utils.*
import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source
import java.util.*

@S2CPacketInfo(id = 0x02, state = ProtocolState.Login)
data class S2CLoginSuccessPacket(
    val uuid: UUID,
    val username: String,
    val properties: List<PlayerProperty>,
    val strictErrorHandling: Boolean,
) : S2CMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeUUID(uuid)
        sink.writeMCString(username)
        sink.writeVarInt(properties.size)
        properties.forEach {
            sink.writeMCString(it.name)
            sink.writeMCString(it.value)
            sink.writeBoolean(it.isSigned)
            if (it.isSigned) {
                sink.writeMCString(it.signature.get())
            }
        }
        sink.writeBoolean(strictErrorHandling)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        // TODO: client side
    }

    companion object {
        fun read(source: Source, packetSize: Int): S2CMCPacket {
            val uuid = source.readUUID()
            val username = source.readMCString()
            val properties = (0 until source.readVarInt()).map {
                val name = source.readMCString()
                val value = source.readMCString()
                val isSigned = source.readBoolean()
                val signature = if (isSigned) {
                    Optional.of(source.readMCString())
                } else {
                    Optional.empty()
                }
                PlayerProperty(name, value, isSigned, signature)
            }
            val strictErrorHandling = source.readBoolean()

            return S2CLoginSuccessPacket(uuid, username, properties, strictErrorHandling)
        }
    }
}