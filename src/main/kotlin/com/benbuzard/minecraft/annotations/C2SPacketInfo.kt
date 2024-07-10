package com.benbuzard.minecraft.annotations

import com.benbuzard.minecraft.protocol.ProtocolState

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class C2SPacketInfo(val id: Int, val state: ProtocolState)
