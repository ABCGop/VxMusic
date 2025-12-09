package com.abcg.music.ui.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.abcg.music.R

data class AppIcon(
    val id: String,
    val name: String,
    val isPro: Boolean,
    val iconResId: Int,
    val componentName: String
)

object IconManager {

    val Default = AppIcon(
        id = "default",
        name = "Classic VxMusic",
        isPro = false,
        iconResId = R.mipmap.ic_launcher_foreground, // Assuming default icon
        componentName = "com.abcg.music.ui.MainActivityDefault"
    )

    val NeonGlow = AppIcon(
        id = "neon_glow",
        name = "Neon Glow",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityNeonGlow"
    )

    val MinimalBlack = AppIcon(
        id = "minimal_black",
        name = "Minimal Black",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityMinimalBlack"
    )

    val GradientSunset = AppIcon(
        id = "gradient_sunset",
        name = "Gradient Sunset",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityGradientSunset"
    )

    val OceanWave = AppIcon(
        id = "ocean_wave",
        name = "Ocean Wave",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityOceanWave"
    )

    val PurpleDream = AppIcon(
        id = "purple_dream",
        name = "Purple Dream",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityPurpleDream"
    )

    val GoldPremium = AppIcon(
        id = "gold_premium",
        name = "Gold Premium",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityGoldPremium"
    )

    val RetroVinyl = AppIcon(
        id = "retro_vinyl",
        name = "Retro Vinyl",
        isPro = true,
        iconResId = R.mipmap.ic_launcher_foreground,
        componentName = "com.abcg.music.ui.MainActivityRetroVinyl"
    )

    val icons = listOf(
        Default,
        NeonGlow,
        MinimalBlack,
        GradientSunset,
        OceanWave,
        PurpleDream,
        GoldPremium,
        RetroVinyl
    )

    fun getIconById(id: String): AppIcon {
        return icons.find { it.id == id } ?: Default
    }

    fun setIcon(context: Context, icon: AppIcon) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        // Disable all other icons
        icons.forEach {
            if (it.componentName != "com.abcg.music.ui.MainActivity") { // Never disable the main activity component itself if it's the default, but we use aliases usually.
                // Actually, usually we have one main activity and aliases.
                // If Default is the main activity, we shouldn't disable it if we want to use aliases.
                // But typically for icon switching, we use aliases for ALL icons including default, OR we keep main enabled and use aliases for others.
                // Best practice: Main Activity is always enabled but has no launcher intent-filter if using aliases.
                // OR: Main Activity is the default, and we enable/disable aliases.
                
                // Let's assume we use aliases for everything including default to be clean, OR default is the base activity.
                // If default is base activity, we can't disable it easily without issues.
                
                // Let's stick to: Default is the base activity.
                // If we switch to an alias, we disable the base activity's launcher alias?
                // No, usually we have <activity-alias> for default too.
                
                val componentName = ComponentName(packageName, it.componentName)
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }

        // Enable selected icon
        val componentName = ComponentName(packageName, icon.componentName)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
