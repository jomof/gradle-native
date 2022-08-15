package com.jomof.cxx

class Constants {
    companion object {
        val PLATFORM_UNKNOWN = 0
        val PLATFORM_LINUX = 1
        val PLATFORM_WINDOWS = 2
        val PLATFORM_DARWIN = 3
        val CURRENT_PLATFORM = currentPlatform()

        fun currentPlatform(): Int {
            val os = System.getProperty("os.name")
            if (os.startsWith("Mac OS")) {
                return PLATFORM_DARWIN
            } else if (os.startsWith("Windows")) {
                return PLATFORM_WINDOWS
            } else if (os.startsWith("Linux")) {
                return PLATFORM_LINUX
            }
            return PLATFORM_UNKNOWN
        }
    }
}