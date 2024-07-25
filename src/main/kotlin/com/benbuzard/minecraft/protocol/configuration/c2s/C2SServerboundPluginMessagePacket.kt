package com.benbuzard.minecraft.protocol.configuration.c2s

import com.benbuzard.minecraft.protocol.C2SMCPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.protocol.utils.*
import com.benbuzard.minecraft.server.MinecraftServer
import com.benbuzard.minecraft.server.entities.ServerPlayer
import io.ktor.network.sockets.*
import okio.Buffer
import okio.Sink
import okio.Source

@C2SPacketInfo(id = 0x02, state = ProtocolState.Configuration)
data class C2SServerboundPluginMessagePacket(
    val channel: Identifier,
    val data: Buffer
) : C2SMCPacket {
    override fun writeData(sink: Sink) {
        sink.writeIdentifier(channel)
        sink.write(data, data.size)
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
        MinecraftServer.instance.pluginChannelRegistry.handle(channel, data, player)
    }

    companion object {
        fun read(source: Source, packetSize: Int): C2SMCPacket {
            val channel = source.readIdentifier()
            val data = MinecraftServer.instance.pluginChannelRegistry.read(channel, source)


            return C2SServerboundPluginMessagePacket(channel, data)
        }
    }
}