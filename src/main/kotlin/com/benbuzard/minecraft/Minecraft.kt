package com.benbuzard.minecraft

import com.benbuzard.minecraft.annotations.C2SPacketInfo
import com.benbuzard.minecraft.logging.Log4j2ConfigurationFactory
import com.benbuzard.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.ConfigurationFactory


fun main() {
    println("Setting logger")

    // Initialize Log4j with our custom configuration
    ConfigurationFactory.setConfigurationFactory(Log4j2ConfigurationFactory())

    println("Logger set")

    val logger = LogManager.getLogger(::main.javaClass)
//    logger.trace("trace")
//    logger.debug("debug")
//    logger.info("info")
//    logger.warn("warn")
//    logger.error("error")
//    logger.fatal("fatal")

    val server = MinecraftServer("0.0.0.0", 25565)
    server.start()

    logger.info("Stopping server...")
}