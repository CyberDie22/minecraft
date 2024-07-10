package com.benbuzard.minecraft.annotations

import com.benbuzard.minecraft.protocol.ProtocolState

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class S2CPacketInfo(val id: Int, val state: ProtocolState)
