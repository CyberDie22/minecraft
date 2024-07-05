package com.benbuzard.minecraft.logging

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.ConfigurationFactory
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Order
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
import org.apache.logging.log4j.core.config.plugins.Plugin
import java.net.URI

@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
class Log4j2ConfigurationFactory : ConfigurationFactory() {

    companion object {
        fun createConfiguration(name: String, builder: ConfigurationBuilder<BuiltConfiguration>): Configuration {
            builder.setConfigurationName(name)
            builder.setStatusLevel(Level.ERROR)
            builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                .addAttribute("level", Level.TRACE))

            val appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
            appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%highlight{%d [%t] %-5level: %msg%n%throwable}{FATAL=red blink, ERROR=red, WARN=yellow bold, INFO=black, DEBUG=green bold, TRACE=blue}")
                .addAttribute("disableAnsi", false))
            appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
                .addAttribute("marker", "FLOW"))
            builder.add(appenderBuilder)

            builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
                .add(builder.newAppenderRef("Stdout"))
                .addAttribute("additivity", false))

            // Add logger for your package
            builder.add(builder.newLogger("com.benbuzard.minecraft", Level.TRACE)
                .add(builder.newAppenderRef("Stdout"))
                .addAttribute("additivity", false))

            builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout")))

            return builder.build()
        }
    }

    override fun getConfiguration(loggerContext: LoggerContext?, source: ConfigurationSource?): Configuration {
        return getConfiguration(loggerContext, source.toString(), null)
    }

    override fun getConfiguration(loggerContext: LoggerContext?, name: String?, configLocation: URI?): Configuration {
        val builder = newConfigurationBuilder()
        return createConfiguration(name ?: "Default", builder)
    }

    override fun getSupportedTypes(): Array<String> = arrayOf("*")
}