@file:OptIn(ExperimentalStdlibApi::class)

package com.benbuzard.minecraft.server.entities

//import com.benbuzard.minecraft.protocol.NetPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.protocol.handshake.c2s.C2SHandshakePacket
import com.benbuzard.minecraft.protocol.status.c2s.C2SPingRequestPacket
import com.benbuzard.minecraft.protocol.status.s2c.S2CPongResponsePacket
import com.benbuzard.minecraft.protocol.status.s2c.S2CStatusResponsePacket
import com.benbuzard.minecraft.protocol.status.s2c.StatusResponseJson
import com.benbuzard.minecraft.protocol.utils.readVarInt
import com.benbuzard.minecraft.server.MinecraftServer
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.io.use
import kotlin.text.HexFormat

class ServerPlayer(val socket: Socket) {
    private var protocolState = ProtocolState.HANDSHAKE

    suspend fun handlePacket(
        source: ByteReadChannel,
        sink: ByteWriteChannel,
        socket: Socket,
        packetId: Int
    ) = coroutineScope {
        val logger = MinecraftServer.instance.logger

        when (protocolState) {
            ProtocolState.HANDSHAKE -> {
                logger.trace("Received packet in handshake state")
                when (packetId) {
                    0x00 -> {
                        val packet = C2SHandshakePacket.read(source)
                        logger.debug { "Packet: $packet" }

                        protocolState = when (packet.nextState) {
                            1 -> ProtocolState.STATUS
                            2 -> ProtocolState.LOGIN
                            3 -> ProtocolState.TRANSFER
                            else -> throw IllegalStateException("Invalid next state ${packet.nextState}")
                        }

                        logger.debug { "Switched protocol state: $protocolState" }
                    }
                    else -> TODO("Unknown packet ID: 0x${packetId.toByte().toHexString(HexFormat.UpperCase)}")
                }
            }
            ProtocolState.STATUS -> {
                logger.trace("Received packet in status state")
                when (packetId) {
                    0x00 -> {
                        logger.debug("Received status request packet")

                        val base64 = this::class.java.getResourceAsStream("/server_icon.png").use { iconStream ->
                            val iconData = iconStream.readAllBytes()
                            return@use Base64.getEncoder().encodeToString(iconData)
                        }

                        val jsonResponse = StatusResponseJson(
                            version = StatusResponseJson.Version(
                                name = "1.21",
                                protocol = 767
                            ),
                            players = StatusResponseJson.Players(
                                max = 100,
                                online = 1,
                                sample = listOf(
                                    StatusResponseJson.Players.Player(
                                        name = "TestPlayer123",
                                        id = UUID.randomUUID().toString()
                                    )
                                )
                            ),
                            description = StatusResponseJson.Description(
                                text = "Hello, world!"
                            ),
                            favicon = "data:image/png;base64,$base64",
                            enforcesSecureChat = false,
                            previewsChat = false,
                        )
                        val jsonResponseString = Json.encodeToString(jsonResponse)

                        val responsePacket = S2CStatusResponsePacket(jsonResponseString)
                        logger.debug { "Writing packet: $responsePacket" }
                        responsePacket.write(sink)
                    }
                    0x01 -> {
                        val packet = C2SPingRequestPacket.read(source)
                        logger.debug { "Packet: $packet" }

                        val responsePacket = S2CPongResponsePacket(packet.payload)
                        logger.debug { "Writing packet: $responsePacket" }
                        responsePacket.write(sink)

                        socket.close()
                    }
                    else -> TODO("Unknown packet ID: 0x${packetId.toByte().toHexString(HexFormat.UpperCase)}")
                }
            }
            ProtocolState.LOGIN -> {
                logger.trace("Received packet in login state")
                when (packetId) {
                    else -> TODO("Unknown packet ID: 0x${packetId.toByte().toHexString(HexFormat.UpperCase)}")
                }
            }
            ProtocolState.TRANSFER -> TODO()
            ProtocolState.PLAY -> TODO()
        }
    }

    suspend fun handle() = coroutineScope {
        launch {
            val logger = MinecraftServer.instance.logger

            val remoteAddress = socket.remoteAddress

            logger.info("Handling client $remoteAddress")
            val recvChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            while (true) {
                if (socket.isClosed) {
                    logger.info("Client $remoteAddress disconnected")
                    break
                }

                if (recvChannel.isClosedForRead) {
                    logger.info("Client $remoteAddress disconnected unexpectedly, ${recvChannel.closedCause}")
                    break
                }

                logger.trace("Waiting for content...")

                recvChannel.awaitContent()

                logger.debug("Content available (${recvChannel.availableForRead} bytes)")

                logger.trace("Reading packet length...")
                val length = recvChannel.readVarInt()
                logger.debug("Packet length: $length")

                logger.trace("Reading packet ID...")
                val packetId = recvChannel.readVarInt()
                logger.debug("Packet ID: ${packetId.toByte().toHexString(HexFormat.UpperCase)}")

                handlePacket(recvChannel, sendChannel, socket, packetId)
            }
        }
    }
}