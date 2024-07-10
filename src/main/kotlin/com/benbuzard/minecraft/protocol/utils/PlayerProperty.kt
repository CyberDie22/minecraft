package com.benbuzard.minecraft.protocol.utils

import java.util.Optional

data class PlayerProperty(
    val name: String,
    val value: String,
    val isSigned: Boolean,
    val signature: Optional<String>
)
