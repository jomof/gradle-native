/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.jomof.cxx

import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class CxxConsumingAgpTest {
    @get:Rule val tempFolder = TemporaryFolder()

    private val projectDir by lazy { tempFolder.root }
    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val appDir by lazy { projectDir.resolve("app") }
    private val libDir by lazy { projectDir.resolve("lib") }
    private val appBuildFile by lazy { appDir.resolve("build.gradle") }
    private val libBuildFile by lazy { libDir.resolve("build.gradle") }
    private val libManifestFile by lazy { libDir.resolve("src/main/AndroidManifest.xml") }
    private val libCMakeListsFile by lazy { libDir.resolve("src/main/cpp/CMakeLists.txt") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test fun `can run task`() {
        appDir.mkdirs()
        libDir.mkdirs()
        libManifestFile.parentFile.mkdirs()
        libCMakeListsFile.parentFile.mkdirs()

        libCMakeListsFile.writeText("""
            cmake_minimum_required(VERSION 3.18.1)
            project("foo")
            add_library(foo SHARED message.c)
            find_library(log-lib log)
            target_link_libraries(foo ${'$'}{log-lib})
        """.trimIndent())

        libDir.resolve("src/main/cpp/message.h").writeText("""
            const char * message();
            
        """.trimIndent())
        libDir.resolve("src/main/cpp/message.c").writeText("""
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
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                }
            }
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }
            include ':lib'
            include ':app'
        """.trimIndent())
        buildFile.writeText("""
            plugins {
                id 'com.github.jomof.cxx.core' version '0.0.1' apply false
                id 'com.android.library' version '7.4.0-alpha09' apply false
                id 'org.jetbrains.kotlin.android' version '1.7.10' apply false
            }

            """.trimIndent())
        libBuildFile.writeText("""
            plugins {
                id 'com.android.library'
                id 'org.jetbrains.kotlin.android'
            }
            
            android {
                namespace 'com.example.myapplication'
                compileSdk 32
            
                buildTypes {
                    debug { }
                    release { }
                }
                externalNativeBuild {
                    cmake {
                        path 'src/main/cpp/CMakeLists.txt'
                        version '3.22.1'
                    }
                }
                buildFeatures {
                    prefabPublishing true
                }
                prefab {
                    foo {
                        libraryName "libfoo"
                    }
                }
            }
            
            dependencies {
//                implementation 'androidx.core:core-ktx:1.7.0'
//                implementation 'androidx.appcompat:appcompat:1.5.0'
//                implementation 'com.google.android.material:material:1.6.1'
//                implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
//                testImplementation 'junit:junit:4.13.2'
//                androidTestImplementation 'androidx.test.ext:junit:1.1.3'
//                androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
            }

            """.trimIndent())

        appBuildFile.writeText("""
            plugins {
               // id 'com.android.library'
                id('com.github.jomof.cxx.core')
            }
//            android {
//                compileSdk 32
//            }
            configurations {
                implementation {
                    //attributes { attribute(usage, 'api') }
                    attributes {
//                        attribute(com.android.build.api.attributes.AgpVersionAttr.ATTRIBUTE, 
//                            objects.named(com.android.build.api.attributes.AgpVersionAttr, "7.4.0-alpha09"))   
                        attribute(com.android.build.api.attributes.BuildTypeAttr.ATTRIBUTE, 
                            objects.named(com.android.build.api.attributes.BuildTypeAttr, "release"))   
                    }
                }
            }
            cxx {
                var prefab = importByAttribute("android-prefab-configuration")
          
                var compile = rule {
                    description = "Building ${'$'}out"
                    depfile = "${'$'}{out}.d"
                    command = "/usr/bin/clang ${'$'}cflags -c ${'$'}in -o ${'$'}out -MD -MF ${'$'}depfile ${'$'}prefab1"
                }
                var linkExe = rule {
                    description = "Linking Executable ${'$'}out"
                    command = "/usr/bin/clang ${'$'}in -o ${'$'}out"
                }
                compile {
                    in = "hello.c"
                    out = "obj/hello.o"
                    cflags = "-I../lib" 
                    prefab1 = prefab
                }
                linkExe {
                    in = [ "obj/hello.o" ]
                    out = "bin/hello"
//                    prefab = prefab
                }
            }
//            configurations {
//                implementation {
//                    canBeConsumed = false
//                    canBeResolved = true
//                    attributes {
//                        attribute(com.android.build.api.attributes.AgpVersionAttr.ATTRIBUTE, 
//                            objects.named(com.android.build.api.attributes.AgpVersionAttr, "7.4.0-alpha09"))   
//                        attribute(com.android.build.api.attributes.BuildTypeAttr.ATTRIBUTE, 
//                            objects.named(com.android.build.api.attributes.BuildTypeAttr, "release"))    
//                    }
//                }
//            }
            dependencies {
                implementation project(":lib")
            }
            """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        //runner.withDebug(true)
        runner.withEnvironment(mapOf("ANDROID_SDK_ROOT" to "/Users/jomof/Library/Android/sdk"))
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("--stacktrace", "app:assemble")
        runner.withProjectDir(projectDir)
        val result = runner.build()
        publishDemo(projectDir, "agp-consuming-cxx",
        """
            TODO
        """.trimIndent())

    }
}
