package com.jomof.cxx

private class CommandLineParserBuffer {
    var buffer = intArrayOf()
}

private val commandLineParserBuffer : ThreadLocal<CommandLineParserBuffer> = ThreadLocal.withInitial { CommandLineParserBuffer() }

fun parseCommandLine(command : String, raw : Boolean = true) : List<String> = with(commandLineParserBuffer.get()) {
    if (buffer.size < minimumSizeOfTokenizeCommandLineBuffer(command)) {
        buffer = allocateTokenizeCommandLineBuffer(command)
    }

    val tokens = TokenizedCommandLine(
        commandLine = command,
        raw = raw,
        indexes = buffer
    )
    tokens.toTokenList()
}

