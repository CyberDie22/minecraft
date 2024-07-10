package com.benbuzard.minecraft.registries

import com.benbuzard.minecraft.ClasspathSearch
import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.annotations.S2CPacketInfo
import com.benbuzard.minecraft.protocol.ProtocolDirection
import com.benbuzard.minecraft.protocol.ProtocolState
import kotlin.reflect.KClass

class PacketRegistry {
    private val packets = mutableMapOf<Triple<Int, ProtocolState, ProtocolDirection>, KClass<*>>()

    init {
        // Load annotated packets

        val c2sPackets = ClasspathSearch.findClassesWithAnnotation(C2SPacketInfo::class, "")
        c2sPackets.forEach { packet ->
            val annotation = packet.annotations.find { it is C2SPacketInfo } as C2SPacketInfo
            registerPacket(annotation.id, annotation.state, ProtocolDirection.C2S, packet)
        }

        val s2cPackets = ClasspathSearch.findClassesWithAnnotation(S2CPacketInfo::class, "")
        s2cPackets.forEach { packet ->
            val annotation = packet.annotations.find { it is S2CPacketInfo } as S2CPacketInfo
            registerPacket(annotation.id, annotation.state, ProtocolDirection.S2C, packet)
        }
    }

    fun getInfo(packet: KClass<*>): Triple<Int, ProtocolState, ProtocolDirection>? {
        return packets.entries.find { it.value == packet }?.key
    }

    fun getId(packet: KClass<*>): Int? = getInfo(packet)?.first

    fun getState(packet: KClass<*>): ProtocolState? = getInfo(packet)?.second

    fun getDirection(packet: KClass<*>): ProtocolDirection? = getInfo(packet)?.third

    fun registerPacket(id: Int, state: ProtocolState, direction: ProtocolDirection, packet: KClass<*>) {
        packets[Triple(id, state, direction)] = packet
    }

    fun getPacket(id: Int, state: ProtocolState, direction: ProtocolDirection): KClass<*>? {
        return packets[Triple(id, state, direction)]
    }
}