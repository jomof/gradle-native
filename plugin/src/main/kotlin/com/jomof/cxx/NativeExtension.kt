package com.jomof.cxx


import groovy.lang.Closure
import org.gradle.api.*
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.*
import java.io.File

abstract class NativePluginExtension(private val project : Project) {
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
                val buildScope = BuildScope(project.configurations)
                buildRuleFactories.add {
                    closure.delegate = buildScope
                    closure.resolveStrategy = Closure.DELEGATE_FIRST
                    project.assertProjectConfigurationsNotResolved()
                    closure.call()
                    project.assertProjectConfigurationsNotResolved {
                        "${buildScope.getProperties()}"
                    }
                    val flagAliases = ConfigurableFlagAliases()
                    val buildCommand = ConfigurableBuildCommand(buildScope.getProperties(), flagAliases)
                    action.delegate = buildCommand
                    action.resolveStrategy = Closure.DELEGATE_FIRST
                    action.call()
                    commandRegistry.add(
                        description = flagAliases.convertToString(buildCommand.properties["description"]!!),
                        inputs = flagAliases.convertToStringList(buildCommand.properties["in"]!!),
                        output = flagAliases.convertToString(buildCommand.properties["out"]!!),
                        command = flagAliases.convertToStringList(buildCommand.properties["command"]!!),
                        depfile = buildCommand.properties["depfile"]?.let { flagAliases.convertToString(it) },
                        flagAliases = flagAliases.aliases
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
    val transform: FlagAlias
)

interface RuleCapture {
    fun call(closure: Closure<Unit>) : BuildScope
}





class ConfigurableBuildCommand(
    initial : Map<String, Any?>,
    val flagAliases : ConfigurableFlagAliases) : ExtraPropertiesExtension {
    private val ext = initial.toMutableMap()
    override fun has(name: String) = ext.contains(name)
    override fun get(name: String) = flagAliases.convertToSpaceSeparated(ext[name] ?: "")
    override fun set(name: String, value: Any?) { ext[name] = value }
    override fun getProperties() = ext
}


class BuildScope(val configurations: ConfigurationContainer) {
    private val ext = mutableMapOf<String, Any?>()
    fun has(name: String) = ext.contains(name)
    fun get(name: String): Any? = ext[name]
    fun set(name: String, value: Any?) { ext[name] = value }
    fun getProperties() : Map<String, Any?> = ext
}

