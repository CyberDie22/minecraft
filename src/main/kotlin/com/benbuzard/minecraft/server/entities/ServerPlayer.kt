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
//    private var packetQueue = mutableListOf<NetPacket>()

    suspend fun handlePacket(
        source: ByteReadChannel,
        sink: ByteWriteChannel,
        packetId: Int,
        packetData: ByteArray
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
                                        name = "Minecraaftt",
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
                        try {
                            responsePacket.write(sink)
                        } catch (e: IllegalStateException) {
                            throw e
                        }
                    }
                    else -> TODO("Unknown packet ID: 0x${packetId.toByte().toHexString(HexFormat.UpperCase)}")
                }
            }
            ProtocolState.LOGIN -> TODO()
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

            val source = recvChannel
            val sink = sendChannel

            // TODO: only works on JVM
//            val source = recvChannel.toInputStream().source().buffer()
//            val sink = sendChannel.toOutputStream().sink().buffer()

//            launch {
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

//                    if (source.exhausted()) {
//                        logger.debug("Client $remoteAddress disconnected unexpectedly")
//                        break
//                    }

                    logger.debug("Content available")

                    logger.trace("Reading packet length...")
                    val length = source.readVarInt()
                    logger.debug("Packet length: $length")

                    logger.trace("Reading packet ID...")
                    val packetId = source.readVarInt()
                    logger.debug("Packet ID: $packetId")

////                    val packetData = Buffer()
//                    val packetData = ByteArray(length - 1)
//                    logger.trace("Reading packet data...")
////                    if (length > 1) source.read(packetData, (length - 1).toLong())
//                    if (length > 1) source.readFully(packetData)
//                    logger.debug("Packet data length: ${packetData.size} bytes")

//                    logger.debug("Adding packet with ID $packetId to queue")
//                    packetQueue.add(NetPacket(packetId, packetData))
                    handlePacket(source, sink, packetId, byteArrayOf())
                }
//            }

//            launch {
//                while (true) {
//                    if (socket.isClosed) {
//                        logger.info("Client $remoteAddress disconnected (handling)")
//                        break
//                    }
//
//                    if (sendChannel.isClosedForWrite) {
//                        logger.info("Client $remoteAddress disconnected unexpectedly (handling), ${sendChannel.closedCause}")
//                        break
//                    }
//
//                    if (packetQueue.isEmpty()) {
//                        continue
//                    }
//
//                    val packet = packetQueue.removeFirst()
//                    logger.debug("Handling packet: $packet")
//                    handlePacket(source, sink, packet.id, packet.data)
//                }
//            }
        }
    }
}