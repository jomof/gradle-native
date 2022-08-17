package com.jomof.cxx


class CommandRegistry {
    private var buffer = intArrayOf()
    private var commands = mutableListOf<BuildCommand>()

    fun all() = commands

    fun add(
        description : Any,
        inputs : Any,
        output : Any,
        command : Any,
        depfile : Any?
    ) {
        val commandLine = convertToSpaceSeparated(command)
        if (buffer.size < minimumSizeOfTokenizeCommandLineBuffer(commandLine)) {
            buffer = allocateTokenizeCommandLineBuffer(commandLine)
        }

        val tokens = TokenizedCommandLine(
            commandLine = commandLine,
            raw = true,
            indexes = buffer
        )

        commands.add(BuildCommand(
            description = description.toString(),
            inputs = convertToStringList(inputs),
            output = output.toString(),
            command = tokens.toTokenList(),
            depfile = depfile?.toString()
        ))
    }
}

class BuildCommand(
    val description : String,
    val inputs : List<String>,
    val output : String,
    val command : List<String>,
    val depfile : String?)

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