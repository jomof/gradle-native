/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.jomof.cxx

import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class ModuleToModuleTest {
    @get:Rule val tempFolder = TemporaryFolder()

    private val projectDir by lazy { tempFolder.root }
    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val appDir by lazy { projectDir.resolve("app") }
    private val libDir by lazy { projectDir.resolve("lib") }
    private val appBuildFile by lazy { appDir.resolve("build.gradle") }
    private val libBuildFile by lazy { libDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test fun `can run task`() {
        appDir.mkdirs()
        libDir.mkdirs()

        libDir.resolve("message.h").writeText("""
            const char * message();
            
        """.trimIndent())
        libDir.resolve("message.c").writeText("""
            const char * message() {
                return "Hello, World!\n";
            }
            
        """.trimIndent())
        appDir.resolve("hello.c").writeText("""
            #include <stdio.h>
            #include "message.h"
            
            int main() {
               printf("%s", message());
               return 0;
            }
            
        """.trimIndent())
        settingsFile.writeText("""
            include ':lib'
            include ':app'
        """.trimIndent())
        buildFile.writeText("""
            plugins {
                id('com.github.jomof.cxx.core') version '0.0.1'
            }

            """.trimIndent())
        appBuildFile.writeText("""
            plugins {
                id('com.github.jomof.cxx.core')
            }
            
            configurations {
                importNativeLibrary {
                    canBeConsumed = false
                    canBeResolved = true
                }
                importNativeHeader {
                    canBeConsumed = false
                    canBeResolved = true
                }
            }
            
            cxx {
                var compile = rule {
                    description = "Building ${'$'}out"
                    depfile = "${'$'}{out}.d"
                    command = "/usr/bin/clang ${'$'}cflags -c ${'$'}in -o ${'$'}out -MD -MF ${'$'}depfile"
                }
                var linkExe = rule {
                    description = "Linking Executable ${'$'}out"
                    command = "/usr/bin/clang ${'$'}in -o ${'$'}out"
                }
                compile {
                    in = "hello.c"
                    out = "obj/hello.o"
                    cflags = configurations.importNativeHeader.files.collect { "-I${'$'}it" }
                }
                linkExe {
                    in = [ "obj/hello.o" ] + configurations.importNativeLibrary
                    out = "bin/hello"
                }
            }
            
            dependencies {
                importNativeLibrary(project(path: ":lib", configuration: 'nativeLibrary'))
                importNativeHeader(project(path: ":lib", configuration: 'nativeHeader'))
            }

            """.trimIndent())

        libBuildFile.writeText("""
            plugins {
                id('com.github.jomof.cxx.core')
            }
            cxx {
                var compile = rule {
                    description = "Building ${'$'}out"
                    depfile = "${'$'}{out}.d"
                    command = "/usr/bin/clang ${'$'}cflags -c ${'$'}in -o ${'$'}out -MD -MF ${'$'}depfile"
                }
                var linkShared = rule {
                    description = "Linking Shared Library ${'$'}out"
                    command = "/usr/bin/clang ${'$'}in -o ${'$'}out -shared"
                }
                compile {
                    in = "message.c"
                    out = "obj/message.o"
                }
                linkShared {
                    in = "obj/message.o"
                    out = "bin/message.so"
                }
            }
            configurations {
                nativeLibrary {
                    canBeConsumed = true
                    canBeResolved = false
                }
                nativeHeader {
                    canBeConsumed = true
                    canBeResolved = false
                }
            }
            artifacts {
                nativeLibrary(file("bin/message.so")) {
                    builtBy("bin-message.so")
                }
                nativeHeader(file("."))
            }
            """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("--configuration-cache", "wrapper", "assemble", "clean")
        runner.withProjectDir(projectDir)
        val result = runner.build()
        publishDemo(projectDir, "module-to-module",
        """
            TODO
        """.trimIndent())

    }
}
