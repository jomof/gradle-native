package com.jomof.cxx

class CommandRegistry {
    private var commands = mutableListOf<BuildCommand>()

    fun all() = commands

    fun add(
        description: String,
        inputs: List<String>,
        output: String,
        command: List<String>,
        depfile: String?,
        flagAliases : Map<String, FlagAlias>
    ) {
        val commandLine = command.flatMap { segment -> parseCommandLine(segment) }
        commands.add(BuildCommand(
            description = description,
            flagAliases = flagAliases,
            inputs = inputs.filter { !flagAliases.containsKey(it) },
            output = output,
            command = commandLine,
            depfile = depfile
        ))
    }
}

class BuildCommand(
    val description: String,
    val flagAliases: Map<String, FlagAlias>,
    val inputs: List<String>,
    val output: String,
    val command: List<String>,
    val depfile: String?
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