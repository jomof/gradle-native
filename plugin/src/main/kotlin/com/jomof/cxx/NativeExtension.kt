package com.jomof.cxx


import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class NativePluginExtension {
    val buildRules = mutableMapOf<String, RuleScope>()
    fun rule(action : Closure<Any>) : RuleCapture {
        return object : RuleCapture {
            override fun call(closure: Closure<Unit>) {
                val buildScope = BuildScope()
                closure.delegate = buildScope
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
                val ruleScope = RuleScope(buildScope.properties)
                action.delegate = ruleScope
                action.resolveStrategy = Closure.DELEGATE_FIRST
                action.call()
                buildRules[buildScope.taskName] = ruleScope
            }
        }
    }
}

data class BuildCommandParameters(
    @get:InputFiles
    val inputs: FileCollection,
    @get:OutputFile
    val output: File,
    @get:Input
    val command: List<String>
)

abstract class BuildTask : DefaultTask() {
    @get:Nested
    abstract val parameters : Property<BuildCommandParameters>

    @TaskAction
    fun build() {
        println(description)
        project.exec { spec ->
            spec.commandLine = parameters.get().command
        }
    }
}

interface RuleCapture {
    fun call(closure: Closure<Unit>)
}

private fun convertToSpaceSeparated(value : Any) : String {
    return when(value) {
        is String -> value
        is ArrayList<*> -> value.joinToString(" ")
        else -> "$value"
    }
}

class RuleScope(initial : Map<String, Any?>) : ExtraPropertiesExtension {
    val description : String get() = get("description")?.toString()!!
    val inputs : List<String> get() = convertToStringList(ext["in"]!!)
    val output : String get() = get("out")?.toString()!!
    val command : String get() = get("command")?.toString()!!
    override fun has(name: String) = ext.contains(name)
    override fun get(name: String): Any? = convertToSpaceSeparated(ext[name] ?: "")
    override fun set(name: String, value: Any?) { ext[name] = value }
    override fun getProperties() = ext
    private val ext = initial.toMutableMap()
}

private fun convertToStringList(value : Any) : List<String> {
   return when(value) {
        is String -> listOf(value)
        is ArrayList<*> -> value.map { "$it" }
        else -> error("Could not convert to List<String> ${value.javaClass}")
    }
}

class BuildScope : ExtraPropertiesExtension {
    val taskName : String get() = get("out")?.toString()?.replace("/", "-") ?: error("No task name")
    override fun has(name: String) = ext.contains(name)
    override fun get(name: String): Any? = ext[name]
    override fun set(name: String, value: Any?) { ext[name] = value }
    override fun getProperties() = ext
    private val ext = mutableMapOf<String, Any?>()
}


