package com.jomof.cxx

import java.io.File


/**
 * Publish a test project as a demo project.
 */
fun publishDemo(projectDir : File, demoName : String, readme : String) {
    println("Publishing $demoName to $projectDir")
    println("")
    val settingsFile = projectDir.resolve("settings.gradle")
    val readmeFile = projectDir.resolve("README.txt")
    readmeFile.writeText(readme)
    val localRepo = File("../../local-plugin-repository")
    settingsFile.writeText("""
            pluginManagement {
                repositories {
                    maven { url '$localRepo' }
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
        """.trimIndent())
    val localCopy = File("../demo/$demoName").absoluteFile.canonicalFile
    println("Publishing to $localCopy")
    localCopy.deleteRecursively()
    localCopy.mkdirs()
    projectDir.copyRecursively(localCopy)
    localCopy.resolve("bin").deleteRecursively()
    localCopy.resolve("obj").deleteRecursively()
    localCopy.resolve("gradlew").setExecutable(true)
}