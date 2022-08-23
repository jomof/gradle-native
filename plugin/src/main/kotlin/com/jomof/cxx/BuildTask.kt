package com.jomof.cxx

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject


@CacheableTask
abstract class BuildTask : DefaultTask() {
    @get:Nested
    abstract val parameters : Property<BuildCommandParameters>

    @get:Inject
    abstract val exec : ExecOperations

    @TaskAction
    fun build() {
        val parameters = parameters.get()
        val sourceFiles = parameters.sourceFiles.associate { it.name to it }

        val command = parameters.command
            .flatMap {
                val source = sourceFiles[it]
                source?.transform?.evaluateAtTaskTime(source.sources) ?: listOf(it)
            }
        val result = exec.exec { spec ->
            spec.commandLine = command
        }
        if (result.exitValue == 0) {
            if (!parameters.output.isFile) error("Expected output ${parameters.output.absoluteFile} to exist")
        }
    }
}
