@file:OptIn(ExperimentalStdlibApi::class)

package com.benbuzard.minecraft.server.entities

import com.benbuzard.minecraft.protocol.ProtocolDirection
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.protocol.utils.*
import com.benbuzard.minecraft.server.MinecraftServer
import io.ktor.network.sockets.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ServerPlayer(val socket: Socket) {
    var protocolState = ProtocolState.Handshake

    suspend fun handle() = coroutineScope {
        launch {
            val logger = MinecraftServer.instance.logger

            val remoteAddress = socket.remoteAddress

            logger.info { "Handling client $remoteAddress" }
            val recvChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            val source = recvChannel.toSource()
            val sink = sendChannel.toSink()

            while (true) {
                if (socket.isClosed) {
                    logger.info { "Client $remoteAddress disconnected" }
                    break
                }

                if (recvChannel.isClosedForRead) {
                    logger.info { "Client $remoteAddress disconnected unexpectedly, ${recvChannel.closedCause}" }
                    break
                }

                logger.trace("Waiting for content...")

                recvChannel.awaitContent()

                logger.debug { "Content available (${recvChannel.availableForRead} bytes)" }

                logger.debug { "Handling packet in $protocolState state" }

                val packet = source.readPacket(this@ServerPlayer, ProtocolDirection.C2S)
                packet.handle(source, sink, this@ServerPlayer)

                logger.debug("Finished handling packet")
            }
        }
    }
}