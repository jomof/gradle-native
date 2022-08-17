package com.jomof.cxx


import groovy.lang.Closure
import groovy.lang.GString
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class NativePluginExtension(val project : Project) {
    val commandRegistry = CommandRegistry()
    val buildCommands by lazy {
        buildRuleFactories.forEach { it() }
        commandRegistry.all()
    }
    private val buildRuleFactories = mutableListOf<() -> Unit>()

    fun rule(action : Closure<Any>) : RuleCapture {
        return object : RuleCapture {
            override fun call(closure: Closure<Unit>) : ExtraPropertiesExtension {
                val buildScope = BuildScope(project.configurations)
                buildRuleFactories.add{
                    closure.delegate = buildScope
                    closure.resolveStrategy = Closure.DELEGATE_FIRST
                    closure.call()
                    val buildCommand = ConfigurarableBuildCommand(buildScope.properties)
                    action.delegate = buildCommand
                    action.resolveStrategy = Closure.DELEGATE_FIRST
                    action.call()
                    commandRegistry.add(
                        description = buildCommand.properties["description"]!!,
                        inputs = buildCommand.properties["in"]!!,
                        output = buildCommand.properties["out"]!!,
                        command = buildCommand.properties["command"]!!,
                        depfile = buildCommand.properties["depfile"]
                    )
                }
                return buildScope
            }
        }
    }
}

data class BuildCommandParameters(
    @get:Input
    val description: String,
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val inputs: FileCollection,
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val dependencies: List<File>,
    @get:OutputFile
    val output: File,
    @get:Input
    val command: List<String>
)

@CacheableTask
abstract class BuildTask : DefaultTask() {
    @get:Nested
    abstract val parameters : Property<BuildCommandParameters>

    @get:Inject
    abstract val exec : ExecOperations

    @TaskAction
    fun build() {
        println(parameters.get().description)
       // println(parameters.get().command.joinToString("\n    "))
        val result = exec.exec { spec ->
            spec.commandLine = parameters.get().command
        }
        if (result.exitValue == 0) {
            if (!parameters.get().output.isFile) error("Expected output ${parameters.get().output.absoluteFile} to exist")
        }
    }
}

interface RuleCapture {
    fun call(closure: Closure<Unit>) : ExtraPropertiesExtension
}

fun convertToSpaceSeparated(value : Any) : String {
    return when(value) {
        is String -> value
        is ArrayList<*> -> value.joinToString(" ")
        else -> "$value"
    }
}

class ConfigurarableBuildCommand(initial : Map<String, Any?>) : ExtraPropertiesExtension {
    override fun has(name: String) = ext.contains(name)
    override fun get(name: String) = convertToSpaceSeparated(ext[name] ?: "")
    override fun set(name: String, value: Any?) { ext[name] = value }
    override fun getProperties() = ext
    private val ext = initial.toMutableMap()
}

fun convertToStringList(value : Any) : List<String> {
   return when(value) {
        is String -> listOf(value)
        is ArrayList<*> -> value.map { "$it" }
        is GString -> listOf("$value")
        else -> error("Could not convert to List<String> ${value.javaClass}")
    }
}

class BuildScope(val configurations: ConfigurationContainer) : ExtraPropertiesExtension {
    override fun has(name: String) = ext.contains(name)
    override fun get(name: String): Any? = ext[name]
    override fun set(name: String, value: Any?) { ext[name] = value }
    override fun getProperties() = ext
    private val ext = mutableMapOf<String, Any?>()
}


