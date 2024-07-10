package com.benbuzard.minecraft.server

import com.benbuzard.minecraft.registries.PacketRegistry
import org.apache.logging.log4j.kotlin.logger
import com.benbuzard.minecraft.server.entities.ServerPlayer
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*

class MinecraftServer(val address: String, val port: Int) {
    init {
        instance = this
    }

    val logger = logger("MinecraftServer")

    val packetRegistry = PacketRegistry()

    private val selectorManager = SelectorManager(Dispatchers.IO)

    private lateinit var serverSocket: ServerSocket

    fun start() = runBlocking {
        serverSocket = aSocket(selectorManager).tcp().bind(address, port)
        logger.info("Minecraft server started on $address:$port")
        listen()
    }

    private suspend fun listen() {
        while (true) {
            val socket = serverSocket.accept()
            logger.info("Accepted connection from ${socket.remoteAddress}")
            val player = ServerPlayer(socket)
            player.handle()
        }
    }

    companion object {
        lateinit var instance: MinecraftServer
    }
}