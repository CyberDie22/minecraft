package com.benbuzard.minecraft.protocol.utils

import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Buffer
import okio.Source

class PluginChannelRegistry {
    private val handlers = mutableMapOf<Identifier, (Buffer, ServerPlayer) -> Unit>()
    private val readers = mutableMapOf<Identifier, (Source) -> Buffer>()

    fun register(channel: Identifier, handler: (Buffer, ServerPlayer) -> Unit, reader: (Source) -> Buffer){
        handlers[channel] = handler
        readers[channel] = reader
    }

    fun handle(channel: Identifier, data: Buffer, player: ServerPlayer) {
        val handler = handlers[channel]
        if (handler != null) {
            handler(data, player)
        }
    }

    fun read(channel: Identifier, source: Source): Buffer {
        val reader = readers[channel]
        if (reader != null) {
            return reader(source)
        }
        throw IllegalArgumentException("No plugin channel reader registered for channel $channel")
    }
}