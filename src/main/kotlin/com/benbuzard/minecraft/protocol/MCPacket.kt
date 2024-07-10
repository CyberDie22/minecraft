package com.benbuzard.minecraft.protocol

import com.benbuzard.minecraft.server.entities.ServerPlayer
import okio.Sink
import okio.Source

interface MCPacket {
    fun writeData(sink: Sink)
    fun handle(source: Source, sink: Sink, player: ServerPlayer)
}