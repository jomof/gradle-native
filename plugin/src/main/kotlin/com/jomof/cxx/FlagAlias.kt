package com.jomof.cxx

import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.*
import java.io.Serializable

interface FlagAlias : Serializable {
    fun expandFlag(prefix: String) = FlagAliasExpansion(this, prefix)
}

class FlagAliasExpansion(
    val child: FlagAlias,
    val prefix: String
) : FlagAlias

class ImportedConfiguration(val attributeValue : String) : FlagAlias

fun FlagAlias.getFileCollection(
    project: Project
) : FileCollection {
    return when(this) {
        is FlagAliasExpansion -> child.getFileCollection(project)
        is ImportedConfiguration -> {
            val result = project.objects.fileCollection()
            project.configurations.forEach { configuration : Configuration ->
                if (configuration.isCanBeResolved) {
                    result.from(configuration.incoming.artifactView { config: ArtifactView.ViewConfiguration ->
                            config.attributes { container: AttributeContainer ->
                                container.attribute(ARTIFACT_TYPE, attributeValue)
                            }
                    }.files)
                }
            }
            result
        }
        else -> error("$javaClass")
    }
}

fun FlagAlias.evaluateAtTaskTime(
    files: FileCollection
) : List<String> {
    return when(this) {
        is FlagAliasExpansion -> files.files.map { "$prefix$it" }
        is ImportedConfiguration -> files.files.map { "$it" }
        else -> error("$javaClass")
    }
}