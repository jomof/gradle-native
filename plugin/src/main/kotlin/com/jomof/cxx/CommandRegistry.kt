package com.jomof.cxx

import org.gradle.api.artifacts.Configuration


class CommandRegistry {
    private var buffer = intArrayOf()
    private var commands = mutableListOf<BuildCommand>()

    fun all() = commands

    fun add(
        description: Any,
        inputs: Any,
        output: Any,
        command: Any,
        depfile: Any?,
        namedEntities: Map<String, Any>,
        referencedConfigurations: List<Configuration>
    ) {
        val namedEntityRecorder = namedEntities.toMutableMap()
        val commandLine = convertToSpaceSeparated(command, namedEntityRecorder)
        if (buffer.size < minimumSizeOfTokenizeCommandLineBuffer(commandLine)) {
            buffer = allocateTokenizeCommandLineBuffer(commandLine)
        }

        val tokens = TokenizedCommandLine(
            commandLine = commandLine,
            raw = true,
            indexes = buffer
        )

        val inputsList = convertToStringList(inputs, namedEntityRecorder)
            .filter { !namedEntityRecorder.containsKey(it) }

        commands.add(BuildCommand(
            description = description.toString(),
            namedEntities = namedEntityRecorder,
            referencedConfigurations = referencedConfigurations,
            inputs = inputsList,
            output = output.toString(),
            command = tokens.toTokenList(),
            depfile = depfile?.toString()
        ))
    }
}

class BuildCommand(
    val description: String,
    val namedEntities: Map<String, Any>,
    val inputs: List<String>,
    val output: String,
    val command: List<String>,
    val depfile: String?,
    val referencedConfigurations: List<Configuration>
)

fun List<String>.removeMatchingElementAndNext(element : String, next : Int) : List<String> {
    if (!contains(element)) return this
    val result = mutableListOf<String>()
    var skipCount = 0
    for(current in this) {
        if (skipCount > 0) {
            --skipCount
            continue
        }
        if (current == element) {
            skipCount = next
            continue
        }
        result.add(current)
    }
    return result
}