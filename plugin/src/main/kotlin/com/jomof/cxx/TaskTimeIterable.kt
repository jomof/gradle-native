package com.jomof.cxx

import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.*
import java.io.Serializable

interface TaskTimeIterable : Serializable {
    fun expandFlag(
        prefix: String
    ) = TaskTimeIterableFlagExpansion(this, prefix)
}

class TaskTimeIterableFlagExpansion(
    val child: TaskTimeIterable,
    val prefix: String
) : TaskTimeIterable

class ImportedConfiguration(val attributeValue : String) : TaskTimeIterable

fun TaskTimeIterable.getFileCollection(
    project: Project
) : FileCollection {
    return when(this) {
        is TaskTimeIterableFlagExpansion -> child.getFileCollection(project)
        is ImportedConfiguration -> {
            val result = project.objects.fileCollection()
            project.configurations.forEach { configuration : Configuration ->
                if (configuration.isCanBeResolved) {
                    result.from(configuration.incoming.artifactView { config: ArtifactView.ViewConfiguration ->
                            config.attributes { container: AttributeContainer ->
                                container.attribute(AndroidArtifacts.ARTIFACT_TYPE, attributeValue)
                            }
                    }.files)
                }
            }
            result
        }
        else -> error("$javaClass")
    }
}

fun TaskTimeIterable.evaluateAtTaskTime(
    files: FileCollection
) : List<String> {
    return when(this) {
        is TaskTimeIterableFlagExpansion -> files.files.map { "$prefix$it" }
        is ImportedConfiguration -> files.files.map { "$it" }
        else -> error("$javaClass")
    }
}