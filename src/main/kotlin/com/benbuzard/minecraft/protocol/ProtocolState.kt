package com.benbuzard.minecraft.protocol

enum class ProtocolState {
    Handshake,
    Status,
    Login,
    Transfer,
    Play
}