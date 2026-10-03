package com.abcg.music.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.abcg.music.data.local.AppLanguage
import com.abcg.music.data.local.SettingsPreferences
import com.abcg.music.data.local.appLocale
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deterministic per-app locale control (Settings -> Language).
 *
 * Deliberately NOT based on AppCompatDelegate: the AppCompat backport only
 * works with an AppCompatActivity context on API <= 32 (per Google's docs),
 * while MainActivity is a FragmentActivity — the call can silently no-op.
 * Instead:
 * - API 33+: framework [LocaleManager] is the authority (this is also what
 *   the system Settings -> App languages screen drives, so both pickers
 *   stay in sync in both directions).
 * - All APIs: [wrap] pins the locale onto every base context in
 *   `attachBaseContext` (Application + MainActivity), so resources resolve
 *   correctly even before DataStore loads and on every recreation.
 * - API < 33: MainActivity observes DataStore and calls `recreate()`.
 *
 * Storage is DataStore (source of truth for the UI) plus a tiny
 * SharedPreferences mirror committed synchronously, so `attachBaseContext`
 * — which runs before any coroutine could deliver DataStore — never reads
 * a stale value, even if the process died mid-write.
 */
@Singleton
class AppLocaleManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val settingsPreferences: SettingsPreferences,
    private val applicationScope: CoroutineScope,
) {
    @Volatile
    private var started = false

    /** Call once from LastWaveApplication.onCreate. Never throws. */
    fun start() {
        if (started) return
        started = true
        applicationScope.launch { adoptSystemLocaleIfNeeded() }
    }

    /**
     * Persist + apply a new language. Safe from any thread; the framework
     * call is marshalled to main (it may restart the Activity).
     */
    fun applyLanguage(language: AppLanguage) {
        applicationScope.launch {
            try {
                settingsPreferences.setAppLanguage(language)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val tag = language.takeUnless { it == AppLanguage.SYSTEM }?.tag
                    withContext(Dispatchers.Main) { setFrameworkLocales(tag) }
                }
                // Pre-33: MainActivity's DataStore observer recreates it.
            } catch (_: Exception) {
                // Locale switch must never crash the app.
            } catch (_: LinkageError) {
            }
        }
    }

    /**
     * API 33+: if the framework holds a different value than DataStore
     * (user changed it via system Settings), adopt it so the in-app picker
     * shows the truth. Public for MainActivity.onCreate refresh.
     */
    suspend fun syncFromSystemIfNeeded() {
        adoptSystemLocaleIfNeeded()
    }

    private suspend fun adoptSystemLocaleIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        try {
            val frameworkTag = withContext(Dispatchers.IO) { readFrameworkTag(appContext) }
            // Empty framework list means "system" — adopt that too, so a
            // system-side reset is reflected instead of sticking.
            val adopted = AppLanguage.fromTag(frameworkTag)
            val current = AppLanguage.fromTag(settingsPreferences.readLanguageTagSync())
            if (adopted != current) {
                settingsPreferences.setAppLanguage(adopted)
            }
        } catch (_: Exception) {
        } catch (_: LinkageError) {
        }
    }

    private fun setFrameworkLocales(tag: String?) {
        try {
            val manager = appContext.getSystemService(LocaleManager::class.java) ?: return
            manager.applicationLocales =
                if (tag.isNullOrBlank()) LocaleList.getEmptyLocaleList()
                else LocaleList.forLanguageTags(tag)
        } catch (_: Exception) {
        } catch (_: LinkageError) {
        }
    }

    companion object {
        /** Null/blank when the framework list is empty (= follow system). */
        fun readFrameworkTag(context: Context): String? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
            return try {
                val manager = context.getSystemService(LocaleManager::class.java)
                    ?: return null
                val list = manager.applicationLocales
                if (list.isEmpty) null else list.toLanguageTags()
            } catch (_: Exception) {
                null
            } catch (_: LinkageError) {
                null
            }
        }

        fun readTagSync(context: Context): String =
            runCatching {
                context.getSharedPreferences(AppLanguage.SYNC_FILE, Context.MODE_PRIVATE)
                    .getString(AppLanguage.SYNC_KEY, AppLanguage.SYSTEM.tag)
            }.getOrNull().let { AppLanguage.fromTag(it).tag }

        /**
         * Effective tag for wrapping: on API 33+ the framework wins (covers
         * changes made via system Settings while the app was alive); below
         * that the synchronous mirror is authoritative.
         */
        fun effectiveTag(context: Context): String {
            val frameworkTag = readFrameworkTag(context)
            if (!frameworkTag.isNullOrBlank()) return AppLanguage.fromTag(frameworkTag).tag
            return readTagSync(context)
        }

        /** Pin the effective locale onto a base context. Never throws. */
        fun wrap(base: Context): Context = wrapWithTag(base, effectiveTag(base))

        fun wrapWithTag(base: Context, rawTag: String?): Context {
            val language = AppLanguage.fromTag(rawTag)
            if (language == AppLanguage.SYSTEM) return base
            return try {
                val locale: Locale = language.appLocale()
                Locale.setDefault(locale)
                val config = Configuration(base.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                base.createConfigurationContext(config)
            } catch (_: Exception) {
                base
            } catch (_: LinkageError) {
                base
            }
        }
    }
}
