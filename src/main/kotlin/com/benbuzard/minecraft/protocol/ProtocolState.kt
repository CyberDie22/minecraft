package com.benbuzard.minecraft.protocol

enum class ProtocolState {
    HANDSHAKE,
    STATUS,
    LOGIN,
    TRANSFER,
    PLAY
}