package com.simpmusic.media_jvm

import com.maxrave.logger.Logger
import com.sun.jna.NativeLibrary
import uk.co.caprica.vlcj.binding.lib.LibC
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil
import uk.co.caprica.vlcj.factory.discovery.strategy.BaseNativeDiscoveryStrategy
import java.io.File

/**
 * Custom NativeDiscoveryStrategy for macOS.
 * On macOS, libvlccore must be force-loaded before libvlc to avoid loading failures.
 *
 * Adapted from https://github.com/mahozad/cutcon MacOsVlcDiscoverer
 * and uk.co.caprica.vlcj.factory.discovery.strategy.OsxNativeDiscoveryStrategy
 */
class MacOsVlcDiscoverer :
    BaseNativeDiscoveryStrategy(
        FILENAME_PATTERNS,
        PLUGIN_PATH_FORMATS,
    ) {
    private val tag = "MacOsVlcDiscoverer"

    override fun supported(): Boolean {
        val os = System.getProperty("os.name", "").lowercase()
        return os.contains("mac")
    }

    override fun discoveryDirectories(): List<String> {
        val path = DefaultVlcDiscoverer.findBundledVlcPath()
        return if (path != null) listOf(path) else emptyList()
    }

    override fun onFound(path: String): Boolean {
        Logger.i(tag, "Found native VLC libraries in $path")
        forceLoadLibVlcCore(path)
        return true
    }

    override fun setPluginPath(pluginPath: String?): Boolean = LibC.INSTANCE.setenv(PLUGIN_ENV_NAME, pluginPath, 1) == 0

    /**
     * On later versions of macOS, it is necessary to force-load libvlccore before libvlc.
     * Otherwise, libvlc will fail to load.
     */
    private fun forceLoadLibVlcCore(path: String) {
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), path)
        NativeLibrary.getInstance(RuntimeUtil.getLibVlcCoreLibraryName())
        Logger.i(tag, "Force-loaded libvlccore from $path")
    }

    companion object {
        private val FILENAME_PATTERNS = arrayOf("libvlc\\.dylib", "libvlccore\\.dylib")
        private val PLUGIN_PATH_FORMATS = arrayOf("%s/plugins")
    }
}