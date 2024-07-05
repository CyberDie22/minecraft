package com.benbuzard.minecraft.protocol

import okio.Buffer

data class NetPacket(
    val id: Int,
    val data: Buffer,
)
