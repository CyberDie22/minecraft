package com.benbuzard.minecraft.protocol.status.c2s

import com.benbuzard.minecraft.protocol.C2SMCPacket
import com.benbuzard.minecraft.protocol.ProtocolState
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.protocol.status.s2c.S2CStatusResponsePacket
import com.benbuzard.minecraft.protocol.status.s2c.StatusResponseJson
import com.benbuzard.minecraft.protocol.utils.writePacket
import com.benbuzard.minecraft.server.entities.ServerPlayer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Sink
import okio.Source
import java.util.*

@C2SPacketInfo(id = 0x00, state = ProtocolState.Status)
class C2SStatusRequestPacket : C2SMCPacket {
    override fun writeData(sink: Sink) {
        // No data to write
    }

    override fun handle(source: Source, sink: Sink, player: ServerPlayer) {
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
        sink.writePacket(responsePacket)
    }

    companion object {
        fun read(source: Source): C2SMCPacket {
            return C2SStatusRequestPacket()
        }
    }
}