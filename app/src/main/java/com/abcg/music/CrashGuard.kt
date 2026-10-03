package com.abcg.music

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Records fatal failures, then delegates to Android to terminate the broken process. */
object CrashGuard {

    private const val TAG = "CrashGuard"
    private const val LOG_FILE_NAME = "lastwave_crash_guard.log"
    private const val MAX_LOG_BYTES = 128L * 1024
    private const val STACK_FRAMES_LOGGED = 32

    @Volatile private var logFile: File? = null
    @Volatile private var installed = false

    @Synchronized
    fun install(context: Context) {
        if (installed) return
        installed = true
        logFile = File(context.applicationInfo.dataDir, LOG_FILE_NAME)
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            appendLog(thread, error)
            Log.e(TAG, "Fatal exception on ${thread.name}", error)
            try {
                previousHandler?.uncaughtException(thread, error)
            } finally {
                hardExit()
            }
        }
    }

    private fun appendLog(thread: Thread, error: Throwable) {
        val file = logFile ?: return
        runCatching {
            synchronized(this@CrashGuard) {
                if (file.length() > MAX_LOG_BYTES) file.writeText("")
                val timestamp = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
                file.appendText(
                    buildString {
                        append(timestamp).append(" [").append(thread.name).append("] ")
                        append(error.javaClass.name).append(": ").append(error.message).append('\n')
                        error.stackTrace.take(STACK_FRAMES_LOGGED)
                            .forEach { append("  at ").append(it).append('\n') }
                        append('\n')
                    },
                )
            }
        }
    }

    private fun hardExit() {
        android.os.Process.killProcess(android.os.Process.myPid())
        Runtime.getRuntime().exit(10)
    }
}
