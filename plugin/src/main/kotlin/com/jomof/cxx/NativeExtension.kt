package com.jomof.cxx


import groovy.lang.Closure
import groovy.lang.GString
import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject
import kotlin.collections.ArrayList

abstract class NativePluginExtension(private val project : Project) {
    private val configurationNames = mutableMapOf<Configuration, String>()
    private val commandRegistry = CommandRegistry()
    val buildCommands by lazy {
        buildRuleFactories.forEach {
            project.assertProjectConfigurationsNotResolved()
            it()
            project.assertProjectConfigurationsNotResolved()
        }
        commandRegistry.all()
    }
    private val buildRuleFactories = mutableListOf<() -> Unit>()
    val configurations : ConfigurationContainer get() = project.configurations

    fun rule(action : Closure<Any>) : RuleCapture {
        return object : RuleCapture {
            override fun call(closure: Closure<Unit>) : BuildScope {
//                var f : FileCollection? = null
//                f!!.elements
                val buildScope = BuildScope(project.configurations)
                buildRuleFactories.add {
                    closure.delegate = buildScope
                    closure.resolveStrategy = Closure.DELEGATE_FIRST
                    project.assertProjectConfigurationsNotResolved()
                    closure.call()
                    project.assertProjectConfigurationsNotResolved {
                        "${buildScope.getProperties()}"
                    }
                    val buildCommand = ConfigurableBuildCommand(buildScope.getProperties())
                    action.delegate = buildCommand
                    action.resolveStrategy = Closure.DELEGATE_FIRST
                    action.call()
                    commandRegistry.add(
                        description = buildCommand.properties["description"]!!,
                        inputs = buildCommand.properties["in"]!!,
                        output = buildCommand.properties["out"]!!,
                        command = buildCommand.properties["command"]!!,
                        depfile = buildCommand.properties["depfile"],
                        namedEntities = buildCommand.namedEntities,
                        referencedConfigurations = listOf()
                    )
                }
                return buildScope
            }
        }
    }

    fun importByAttribute(type : String) : ImportedConfiguration {
        return ImportedConfiguration(type)
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
    val dependencies: ConfigurableFileCollection,
    @get:OutputFile
    val output: File,
    @get:Input
    val command: List<String>,
    @get:Nested
    val sourceFiles: List<SourceFiles>
)

data class SourceFiles(
    @get:Input
    val name: String,
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sources : FileCollection,
    @get:Input
    val transform: TaskTimeIterable
)

interface RuleCapture {
    fun call(closure: Closure<Unit>) : BuildScope
}

fun convertToString(value : Any, namedEntityRecorder : MutableMap<String, Any>) : String {
    return when(value) {
        is String -> value
        is GString -> "$value"
        is Closure<*> -> convertToString(value.call(), namedEntityRecorder)
        is Buildable -> {
            val name = "buildable://${namedEntityRecorder.keys.size}"
            namedEntityRecorder[name] = value
            name
        }
        is TaskTimeIterable -> {
            val name = "iterable://${namedEntityRecorder.keys.size}"
            namedEntityRecorder[name] = value
            name
        }
        else -> error("$value : ${value.javaClass}")
    }
}

fun convertToSpaceSeparated(value : Any, namedEntityRecorder : MutableMap<String, Any>) : String {
    return when(value) {
        is String -> value
        is ArrayList<*> -> value.joinToString(" ") { convertToString(it, namedEntityRecorder) }
        is GString -> "$value"
        is Buildable -> {
            val name = "buildable://${namedEntityRecorder.keys.size}"
            namedEntityRecorder[name] = value
            name
        }
        is TaskTimeIterable -> {
            val name = "iterable://${namedEntityRecorder.keys.size}"
            namedEntityRecorder[name] = value
            name
        }
        else -> error(value.javaClass)
    }
}

fun convertToStringList(value : Any, namedEntityRecorder : MutableMap<String, Any>) : List<String> {
    return when(value) {
        is String -> listOf(value)
        is ArrayList<*> -> value.map { convertToString(it, namedEntityRecorder) }
        is GString -> listOf("$value")
        else -> error("Could not convert to List<String> ${value.javaClass}")
    }
}


class ConfigurableBuildCommand(initial : Map<String, Any?>) : ExtraPropertiesExtension {
    val namedEntities = mutableMapOf<String, Any>()
    override fun has(name: String) = ext.contains(name)
    override fun get(name: String) = convertToSpaceSeparated(ext[name] ?: "", namedEntities)
    override fun set(name: String, value: Any?) { ext[name] = value }
    override fun getProperties() = ext
    private val ext = initial.toMutableMap()
}


class BuildScope(val configurations: ConfigurationContainer) {
    private val ext = mutableMapOf<String, Any?>()
    fun has(name: String) = ext.contains(name)
    fun get(name: String): Any? = ext[name]
    fun set(name: String, value: Any?) { ext[name] = value }
    fun getProperties() : Map<String, Any?> = ext
}

