/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Gradle plugin project to get you started.
 * For more details take a look at the Writing Custom Plugins chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.5.1/userguide/custom_plugins.html
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.0.0"

    id("com.google.cloud.artifactregistry.gradle-plugin") version "2.1.5"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest()
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest()

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project)
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) } 
                }
            }
        }
    }
}

group = "com.github.jomof"
version = "0.0.2"

gradlePlugin {
    val `native` by plugins.creating {
        id = "com.github.jomof.cxx.core"
        implementationClass = "com.jomof.cxx.NativePlugin"
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("functionalTest"))
}

pluginBundle {
    website = "https://github.com/jomof/gradle-native"
    vcsUrl = "https://github.com/jomof/gradle-native"
    tags = listOf("gradle", "C++", "C", "native")
}

publishing {
    repositories {
//        maven {
//            name = "localPluginRepository"
//            url = uri("../local-plugin-repository")
//        }
        maven {
            name = "jomof-github-maven"
            url = uri("artifactregistry://us-maven.pkg.dev/jomof-github/maven")
        }
    }
}
