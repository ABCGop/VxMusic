package com.simpmusic.media_jvm

import com.maxrave.logger.Logger
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy
import java.io.File

/**
 * Custom NativeDiscoveryStrategy for Windows and Linux.
 * Discovers bundled VLC native libraries from compose.application.resources.dir.
 *
 * Adapted from https://github.com/mahozad/cutcon DefaultVlcDiscoverer
 */
class DefaultVlcDiscoverer : NativeDiscoveryStrategy {

    private val tag = "DefaultVlcDiscoverer"

    override fun supported(): Boolean {
        val os = System.getProperty("os.name", "").lowercase()
        // Supported on everything except macOS (handled by MacOsVlcDiscoverer)
        return !os.contains("mac")
    }

    override fun discover(): String? {
        return findBundledVlcPath()
    }

    override fun onFound(path: String): Boolean {
        Logger.i(tag, "Found native VLC libraries in $path")
        return true
    }

    override fun onSetPluginPath(path: String): Boolean {
        Logger.i(tag, "VLC plugin path set to $path")
        return true
    }

    companion object {
        private const val TAG = "DefaultVlcDiscoverer"

        /**
         * Find bundled VLC native libraries path.
         * Search order:
         * 1. compose.application.resources.dir (packaged app)
         * 2. vlc.bundled.path system property (dev mode, set by Gradle)
         * 3. Relative vlc-natives/<os> fallback
         */
        fun findBundledVlcPath(): String? {
            // 1. Packaged app: compose.application.resources.dir
            val resourcesDir = System.getProperty("compose.application.resources.dir")
            if (resourcesDir != null) {
                val found = findVlcInDirectory(File(resourcesDir))
                if (found != null) return found
            }

            // 2. Dev mode: vlc.bundled.path set by Gradle run task
            val bundledPath = System.getProperty("vlc.bundled.path")
            if (bundledPath != null) {
                val dir = File(bundledPath)
                if (dir.exists() && hasVlcLib(dir)) {
                    Logger.i(TAG, "Found VLC via vlc.bundled.path: $bundledPath")
                    return dir.absolutePath
                }
            }

            // 3. Fallback: relative to working directory
            val osName = System.getProperty("os.name", "").lowercase()
            val subDir = when {
                osName.contains("win") -> "windows"
                osName.contains("mac") -> "macos"
                else -> "linux"
            }
            val fallbackDir = File("vlc-natives/$subDir")
            if (fallbackDir.exists() && hasVlcLib(fallbackDir)) return fallbackDir.absolutePath

            return null
        }

        private fun findVlcInDirectory(dir: File): String? {
            if (!dir.exists() || !dir.isDirectory) return null
            if (hasVlcLib(dir)) return dir.absolutePath
            // Check subdirectories (vlc-setup may organize by OS)
            dir.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
                if (hasVlcLib(subDir)) return subDir.absolutePath
            }
            return null
        }

        private fun hasVlcLib(dir: File): Boolean =
            dir.listFiles()?.any {
                it.name.startsWith("libvlc") || it.name == "vlc.dll"
            } == true
    }
}
