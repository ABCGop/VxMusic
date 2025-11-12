package com.abcg.music.util

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.tasks.await

class PlayStoreUpdateManager(private val activity: Activity) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var updateInfo: AppUpdateInfo? = null
    
    companion object {
        private const val TAG = "PlayStoreUpdateManager"
        const val REQUEST_CODE_UPDATE = 1001
    }
    
    private val installStateUpdatedListener: InstallStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update downloaded, ready to install")
                // Notify user that update is ready to install
                completeUpdate()
            }
            InstallStatus.DOWNLOADING -> {
                Log.d(TAG, "Update downloading...")
            }
            InstallStatus.FAILED -> {
                Log.e(TAG, "Update failed with error code: ${state.installErrorCode()}")
            }
            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed successfully")
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            }
            else -> {
                Log.d(TAG, "Update status: ${state.installStatus()}")
            }
        }
    }
    
    /**
     * Check if an update is available
     * @return Pair<Boolean, AppUpdateType> - First is whether update is available, second is the update type
     */
    suspend fun checkForUpdate(): Pair<Boolean, Int?> {
        return try {
            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
            updateInfo = appUpdateInfo
            
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            
            if (isUpdateAvailable) {
                // Determine update type based on priority
                val updateType = if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateType.IMMEDIATE
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    AppUpdateType.FLEXIBLE
                } else {
                    null
                }
                
                Log.d(TAG, "Update available: $isUpdateAvailable, type: $updateType")
                Pair(isUpdateAvailable, updateType)
            } else {
                Log.d(TAG, "No update available")
                Pair(false, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for update", e)
            Pair(false, null)
        }
    }
    
    /**
     * Start the update flow
     * @param updateType AppUpdateType.IMMEDIATE or AppUpdateType.FLEXIBLE
     */
    fun startUpdate(updateType: Int): Boolean {
        val info = updateInfo ?: return false
        
        return try {
            if (updateType == AppUpdateType.FLEXIBLE) {
                appUpdateManager.registerListener(installStateUpdatedListener)
            }
            
            val options = AppUpdateOptions.newBuilder(updateType).build()
            appUpdateManager.startUpdateFlowForResult(
                info,
                activity,
                options,
                REQUEST_CODE_UPDATE
            )
            true
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Error starting update flow", e)
            false
        }
    }
    
    /**
     * Complete a flexible update
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
    
    /**
     * Handle activity result for update flow
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_CODE_UPDATE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "Update flow successful")
                    return true
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "Update flow cancelled by user")
                    return false
                }
                else -> {
                    Log.e(TAG, "Update flow failed with result code: $resultCode")
                    return false
                }
            }
        }
        return false
    }
    
    /**
     * Check if an in-progress update is available
     */
    suspend fun checkInProgressUpdate(): Boolean {
        return try {
            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
            appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        } catch (e: Exception) {
            Log.e(TAG, "Error checking in-progress update", e)
            false
        }
    }
    
    /**
     * Resume an in-progress update
     */
    suspend fun resumeUpdateIfNeeded() {
        try {
            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
            
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    options,
                    REQUEST_CODE_UPDATE
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming update", e)
        }
    }
    
    /**
     * Clean up listeners
     */
    fun cleanup() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}
