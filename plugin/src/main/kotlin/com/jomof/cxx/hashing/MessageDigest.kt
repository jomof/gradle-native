/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jomof.cxx

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Apply [String] to the [MessageDigest] hash.
 */
fun MessageDigest.update(string : String) {
    val stream = ByteArrayInputStream(string.toByteArray(StandardCharsets.UTF_8))
    val block = ByteArray(16)
    var length = stream.read(block)
    while (length > 0) {
        update(block, 0, length)
        length = stream.read(block)
    }
}

/**
 * Convert [MessageDigest] to base-36 [String].
 */
fun MessageDigest.toBase36(): String {
    val sb = StringBuilder()
    for (byte in digest()) {
        val signExtended = byte - Byte.MIN_VALUE
        sb.append(signExtended.toString(Character.MAX_RADIX))
    }
    return sb.toString()
}