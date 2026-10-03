package com.abcg.music.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val isChecking: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val latestVersion: String = "",
    val currentVersion: String = "",
    val releaseNotes: String = "",
    val releaseUrl: String = "",
    val downloadUrl: String? = null,
    val isDismissed: Boolean = false,
    val message: String? = null,
)

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("vxmusic_updates", Context.MODE_PRIVATE)
    private val playUpdateManager = AppUpdateManagerFactory.create(context)

    private val _updateInfo = MutableStateFlow(
        UpdateInfo(
            currentVersion = getCurrentVersion(),
            releaseUrl = "https://play.google.com/store/apps/details?id=${context.packageName}",
            downloadUrl = "market://details?id=${context.packageName}",
        )
    )
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    init {
        checkForUpdate(isSilent = true)
    }

    fun getCurrentVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "4.1.0"
    } catch (_: Exception) {
        "4.1.0"
    }

    fun checkForUpdate(isSilent: Boolean = false) {
        _updateInfo.update {
            it.copy(
                isChecking = true,
                message = if (!isSilent) "Checking Google Play Store..." else it.message,
            )
        }

        playUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                val currentVersion = getCurrentVersion()
                val isAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val availableVersionCode = appUpdateInfo.availableVersionCode()
                val latestVer = if (isAvailable) "v$currentVersion.$availableVersionCode" else currentVersion
                val dismissedVersion = prefs.getString("dismissed_version", null)
                val isDismissed = dismissedVersion == latestVer

                _updateInfo.update {
                    it.copy(
                        isChecking = false,
                        isUpdateAvailable = isAvailable,
                        latestVersion = latestVer,
                        currentVersion = currentVersion,
                        releaseUrl = "https://play.google.com/store/apps/details?id=${context.packageName}",
                        downloadUrl = "market://details?id=${context.packageName}",
                        isDismissed = isDismissed,
                        message = if (!isSilent) {
                            if (isAvailable) "New version available on Google Play!" else "You're on the latest version ($currentVersion)"
                        } else null,
                    )
                }
            }
            .addOnFailureListener { e ->
                val currentVersion = getCurrentVersion()
                _updateInfo.update {
                    it.copy(
                        isChecking = false,
                        message = if (!isSilent) "Play Store: you're on version $currentVersion" else null,
                    )
                }
            }
    }

    fun dismissUpdate(version: String) {
        val clean = version.removePrefix("v").removePrefix("V")
        prefs.edit().putString("dismissed_version", clean).apply()
        _updateInfo.update { it.copy(isDismissed = true) }
    }

    fun openUpdate(context: Context) {
        val packageName = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            try {
                context.startActivity(webIntent)
            } catch (_: Exception) { }
        }
    }

    fun isNewerVersion(remote: String, local: String): Boolean {
        if (remote.isBlank() || local.isBlank()) return false
        val cleanRemote = remote.removePrefix("v").removePrefix("V").substringBefore("-")
        val cleanLocal = local.removePrefix("v").removePrefix("V").substringBefore("-")
        if (cleanRemote == cleanLocal) return false

        val rParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
