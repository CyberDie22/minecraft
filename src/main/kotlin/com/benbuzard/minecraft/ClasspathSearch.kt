package com.benbuzard.minecraft

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass

object ClasspathSearch {
    fun findClassesWithAnnotation(annotation: KClass<out Annotation>, packageName: String): Set<KClass<*>> {
        val configBuilder = ConfigurationBuilder()
            .setScanners(Scanners.TypesAnnotated)

        if (packageName == "") {
            configBuilder.setUrls(ClasspathHelper.forJavaClassPath())
        } else {
            configBuilder.forPackages(packageName)
        }

        val reflections = Reflections(configBuilder)
        return reflections.getTypesAnnotatedWith(annotation.java).map { it.kotlin }.toSet()
    }
}