package com.jomof.cxx

import java.io.File


/**
 * Publish a test project as a demo project.
 */
fun publishDemo(projectDir : File, demoName : String, readme : String) {
    val localCopy = File("../demo/$demoName").absoluteFile.canonicalFile
    println("Publishing $demoName to $localCopy")
    println("")
    val settingsFile = projectDir.resolve("settings.gradle")
    val readmeFile = projectDir.resolve("README.txt")
    readmeFile.writeText(readme)
    val gitignoreFile = projectDir.resolve(".gitignore")
    gitignoreFile.writeText("""
        obj/
        bin/
        .cxx/
        build/
    """.trimIndent())
    readmeFile.writeText(readme)
    val localRepo = File("../../local-plugin-repository")
    val priorSettings = settingsFile.readText().trimIndent()
    settingsFile.writeText("""
            pluginManagement {
                repositories {
                    maven { url '$localRepo' }
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            
        """.trimIndent() + priorSettings)

    localCopy.deleteRecursively()
    localCopy.mkdirs()
    projectDir.copyRecursively(localCopy)
    localCopy.resolve("bin").deleteRecursively()
    localCopy.resolve("app/bin").deleteRecursively()
    localCopy.resolve("lib/bin").deleteRecursively()
    localCopy.resolve("obj").deleteRecursively()
    localCopy.resolve("app/obj").deleteRecursively()
    localCopy.resolve("lib/obj").deleteRecursively()
    localCopy.resolve("gradlew").setExecutable(true)
}