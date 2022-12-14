/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.jomof.cxx

import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

class SimplestPrecompiledHeaderProjectTest {
    @get:Rule val tempFolder = TemporaryFolder()

    private val projectDir by lazy { tempFolder.root }
    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test fun `can run task`() {
        projectDir.resolve("obj/hello.o.d").delete()
        projectDir.resolve("hello.h").writeText("""
            #define MESSAGE "Hello, World!\n"
            
        """.trimIndent())
        projectDir.resolve("common.h").writeText("""
            #include <stdio.h>
            #include "hello.h"
            
        """.trimIndent())
        projectDir.resolve("hello.c").writeText("""
            #include "common.h"
            
            int main() {
               printf(MESSAGE);
               return 0;
            }
            
        """.trimIndent())
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id('com.github.jomof.cxx.core') version '0.0.1'
            }
            cxx {
                var pch = rule {
                    description = "Building PCH ${'$'}out"
                    depfile = "${'$'}{out}.d"
                    command = "/usr/bin/clang -x c-header ${'$'}in -o ${'$'}out -MD -MF ${'$'}depfile"
                }
                var compile = rule {
                    description = "Building ${'$'}out"
                    depfile = "${'$'}{out}.d"
                    command = "/usr/bin/clang -include-pch ${'$'}pch ${'$'}cflags -c ${'$'}source -o ${'$'}out -MD -MF ${'$'}depfile"
                }
                var link = rule {
                    description = "Linking ${'$'}out"
                    command = "/usr/bin/clang ${'$'}pch ${'$'}in -o ${'$'}out"
                }
                var common = pch {
                    in = "common.h"
                    out = "obj/common.h.pch"
                }
                var hello = compile {
                    source = "hello.c"
                    pch = common.out
                    in = [ source, pch ]
                    out = "obj/hello.o"
                    cflags = "-Weverything"
                }
                link {
                    in = hello.out
                    out = "bin/hello"
                }
            }
            """.trimIndent())

        // Run the build"
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("--stacktrace", "--configuration-cache", "wrapper", "bin-hello", "clean")
        runner.withProjectDir(projectDir)
        val result = runner.build()
        publishDemo(projectDir, "simplest-pch",
            """
                This project demonstrates creating and using a precompiled header file.
            """.trimIndent())
    }
}
