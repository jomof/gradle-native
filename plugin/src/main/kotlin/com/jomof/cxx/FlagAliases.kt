package com.jomof.cxx

import groovy.lang.Closure
import groovy.lang.GString

class ConfigurableFlagAliases {
    private val mutableAliases = mutableMapOf<String, FlagAlias>()
    val aliases : Map<String, FlagAlias> = mutableAliases

    fun convertToSpaceSeparated(value : Any) : String {
        return when(value) {
            is String -> value
            is ArrayList<*> -> value.joinToString(" ") { convertToString(it) }
            is GString -> "$value"
            is FlagAlias -> {
                val name = "flag-alias://${aliases.keys.size}"
                mutableAliases[name] = value
                name
            }
            else -> error(value.javaClass)
        }
    }

    fun convertToStringList(value : Any) : List<String> {
        return when(value) {
            is String -> listOf(value)
            is ArrayList<*> -> value.map { convertToString(it) }
            is GString -> listOf("$value")
            else -> error("Could not convert to List<String> ${value.javaClass}")
        }
    }

   fun convertToString(value : Any) : String {
        return when(value) {
            is String -> value
            is GString -> "$value"
            is Closure<*> -> convertToString(value.call())
            is FlagAlias -> {
                val name = "flag-alias://${aliases.keys.size}"
                mutableAliases[name] = value
                name
            }
            else -> error("$value : ${value.javaClass}")
        }
    }
}