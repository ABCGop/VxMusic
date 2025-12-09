package com.abcg.music

import android.content.Context
import android.util.Log
// Sentry disabled - imports commented out
// import io.sentry.Sentry
// import io.sentry.android.core.SentryAndroid

// Sentry disabled - stub implementation
fun reportCrash(throwable: Throwable) {
    Log.w("Crashlytics", "Sentry disabled - crash not reported: ${throwable.message}")
    // Sentry.captureException(throwable)
}

fun configCrashlytics(applicationContext: Context) {
    Log.i("Crashlytics", "Sentry disabled - crashlytics not initialized")
    /*
    SentryAndroid.init(applicationContext) { options ->
        val dsn = BuildConfig.SENTRY_DSN
        Log.d("Sentry", "dsn: $dsn")
        options.dsn = dsn
    }
    */
}

fun pushPlayerError(error: Throwable) {
    Log.w("Crashlytics", "Sentry disabled - player error not reported: ${error.message}")
    /*
    Sentry.withScope { scope ->
        Sentry.captureException(error)
    }
    */
}
