package com.benbuzard.minecraft.protocol.status.s2c

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponseJson(
    val version: Version,
    val players: Players,
    val description: Description,
    val favicon: String,
    val enforcesSecureChat: Boolean,
    val previewsChat: Boolean,
) {
    @Serializable
    data class Version(
        val name: String,
        val protocol: Int,
    )

    @Serializable
    data class Players(
        val max: Int,
        val online: Int,
        val sample: List<Player>?,
    ) {
        @Serializable
        data class Player(
            val name: String,
            val id: String,
        )
    }

    @Serializable
    data class Description(
        val text: String,
    )
}
