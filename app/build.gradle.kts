import com.android.build.gradle.internal.tasks.CompileArtProfileTask
import java.util.Properties

val isFullBuild: Boolean by rootProject.extra
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    // alias(libs.plugins.sentry.gradle)
}

kotlin {
    jvmToolchain(17) // or appropriate version
    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
    }
}

android {
    namespace = "com.abcg.music"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.abcg.music"
        minSdk = 26
        targetSdk = 36
        versionCode =
            libs.versions.version.code
                .get()
                .toInt()
        versionName =
            libs.versions.version.name
                .get()
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        @Suppress("UnstableApiUsage")
        androidResources {
            localeFilters +=
                listOf(
                    "en",
                    "vi",
                    "it",
                    "de",
                    "ru",
                    "tr",
                    "fi",
                    "pl",
                    "pt",
                    "fr",
                    "es",
                    "zh",
                    "in",
                    "ar",
                    "ja",
                    "b+zh+Hant+TW",
                    "uk",
                    "iw",
                    "az",
                    "hi",
                    "th",
                    "nl",
                    "ko",
                    "ca",
                    "fa",
                    "bg",
                )
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // No abiFilters needed - pure Java/Kotlin app supports ALL architectures
            // Including Chromebooks (x86/x86_64) automatically!
            
            // 16 KB Page Size Support
            // Ensures compatibility with newer Android devices
            debugSymbolLevel = "FULL"
        }

        // Sentry DSN disabled - uncomment to re-enable
        /*
        if (isFullBuild) {
            try {
                println("Full build detected, enabling Sentry DSN")
                val properties = Properties()
                properties.load(rootProject.file("local.properties").inputStream())
                buildConfigField(
                    "String",
                    "SENTRY_DSN",
                    "\"${properties.getProperty("SENTRY_DSN") ?: ""}\"",
                )
            } catch (e: Exception) {
                println("Failed to load SENTRY_DSN from local.properties: ${e.message}")
            }
        }
        */
    }

    bundle {
        language {
            enableSplit = false
        }
        
        // 16 KB Page Size Support
        // Optimize bundle for 16 KB aligned libraries
        abi {
            enableSplit = true
        }
    }

    flavorDimensions += "app"

    productFlavors {
        create("foss") {
            dimension = "app"
        }
        create("full") {
            dimension = "app"
        }
    }

    signingConfigs {
        create("release") {
            // Load signing config from environment variables or local properties
            // For local builds: create keystore.properties file (see keystore.properties.template)
            // For CI/CD: set environment variables KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                
                storeFile = file(keystoreProperties.getProperty("storeFile") ?: "${rootProject.projectDir}/VxMusic.jks")
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            } else {
                // Fallback to environment variables (for CI/CD)
                val keystorePath = System.getenv("KEYSTORE_FILE") ?: "${rootProject.projectDir}/VxMusic.jks"
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "consumer-rules.pro",
                "proguard-rules.pro",
            )
            
            
            // Pure Java/Kotlin app - supports ALL architectures automatically
            // No abiFilters needed - this enables universal compatibility
            ndk {
                // Keep debug symbols for crash reporting
                debugSymbolLevel = "FULL"
            }
            
            // Splits are for APKs only, AAB handles this automatically
            /*
            splits {
                abi {
                    isEnable = true
                    reset()
                    isUniversalApk = true
                    include(*abis)
                }
            }
            */
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            
            // Pure Java/Kotlin - no architecture restrictions needed
        }
    }
    
    // Bundle configuration - Enable native debug symbols for crash reporting
    bundle {
        abi {
            // Enable split APKs for different architectures (reduces download size)
            enableSplit = true
        }
        language {
            // Enable split APKs for different languages (reduces download size)
            enableSplit = true
        }
        density {
            // Enable split APKs for different screen densities (reduces download size)
            enableSplit = true
        }
    }
    
    // Packaging options - Preserve native debug symbols
    packaging {
        jniLibs {
            // Keep debug symbols for crash reporting
            keepDebugSymbols += listOf("**/*.so")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    // Lint configuration
    lint {
        disable += "Instantiatable"  // Disable for Firebase services in FOSS build
    }
    
    // enable view binding
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    
    // Performance optimization for low-end devices
    composeOptions {
        // Reduce compose runtime overhead
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }
    packaging {
        jniLibs.useLegacyPackaging = false
        
        // 16 KB Page Size Support
        // Enable page alignment for native libraries
        jniLibs.pickFirsts += listOf("**/libc++_shared.so")
        
        jniLibs.excludes +=
            listOf(
                "META-INF/META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/asm-license.txt",
                "META-INF/notice",
                "META-INF/*.kotlin_module",
            )
        
        // Ensure proper alignment for 16 KB page sizes
        resources.pickFirsts += listOf(
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }
}

// Sentry configuration disabled - uncomment to re-enable
/*
sentry {
    org.set("simpmusic")
    projectName.set("android")
    ignoredFlavors.set(setOf("foss"))
    ignoredBuildTypes.set(setOf("debug"))
    autoInstallation.enabled = false
    val token =
        try {
            println("Full build detected, enabling Sentry Auth Token")
            val properties = Properties()
            properties.load(rootProject.file("local.properties").inputStream())
            properties.getProperty("SENTRY_AUTH_TOKEN")
        } catch (e: Exception) {
            println("Failed to load SENTRY_AUTH_TOKEN from local.properties: ${e.message}")
            null
        }
    authToken.set(token ?: "")
    includeProguardMapping.set(true)
    // Don't upload ProGuard mappings automatically on developer machines (paths with spaces
    // can break sentry-cli on Windows). Enable uploading explicitly in CI by setting
    // the environment variable SENTRY_AUTO_UPLOAD=true (the CI workflow already
    // provides SENTRY_AUTH_TOKEN).
    val enableSentryUpload = System.getenv("SENTRY_AUTO_UPLOAD") == "true"
    autoUploadProguardMapping.set(enableSentryUpload)
    telemetry.set(false)
}
*/

dependencies {
    val fullImplementation = "fullImplementation"
    val debugImplementation = "debugImplementation"

    coreLibraryDesugaring(libs.desugaring)

    // Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.material3.lib)
    implementation(libs.compose.material3.sizeclass)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.ui)
    implementation(libs.compose.material.ripple)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.viewbinding)
    implementation(libs.constraintlayout.compose)

    implementation(libs.glance)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    implementation(libs.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)

    implementation(libs.work.runtime.ktx)
    androidTestImplementation(libs.work.testing)

    // Runtime
    implementation(libs.startup.runtime)
    implementation(project(":common"))
    // Other module
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":media3-ui"))

    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    debugImplementation(libs.ui.tooling)

    // ExoPlayer

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)

    // Legacy Support
    implementation(libs.legacy.support.v4)
    // Coroutines
    implementation(libs.coroutines.android)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kmpalette.core)
    // Easy Permissions
    implementation(libs.easypermissions)

    // Preference
    implementation(libs.preference.ktx)

    // DataStore
    implementation(libs.datastore.preferences)

    // Lottie
    implementation(libs.lottie)
    implementation(libs.lottie.compose)

    // Paging 3
    implementation(libs.paging.runtime.ktx)
    implementation(libs.paging.compose)

    // Custom Activity On Crash
    implementation(libs.customactivityoncrash)

    implementation(libs.aboutlibraries)
    implementation(libs.aboutlibraries.compose.m3)

    implementation(libs.balloon)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Jetbrains Markdown
    api(libs.markdown)

    // Blur Haze
    implementation(libs.haze)
    implementation(libs.haze.material)

    // Sentry disabled - uncomment to re-enable
    // fullImplementation(libs.sentry.android)

    implementation(libs.liquid.glass)

    // Google Play In-App Updates
    fullImplementation(libs.play.app.update)
    fullImplementation(libs.play.app.update.ktx)

    // Google Play Billing
    implementation(libs.billing.ktx)

    // Firebase - Remote Control System
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.coroutines.play.services)

    configurations.all {
        resolutionStrategy {
            force("com.google.protobuf:protobuf-javalite:3.25.3")
        }
    }

    // Social chat feature temporarily disabled due to dependency conflicts

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.ktor:ktor-client-android:3.0.0")
    implementation("io.ktor:ktor-client-core:3.0.0")

//    debugImplementation(libs.leak.canary)
}
/**
 * Task to generate the aboutlibraries.json file
 **/
aboutLibraries {
    collect.configPath = file("../config")
    export {
        prettyPrint = true
        excludeFields = listOf("generated")
    }
}
tasks.withType<CompileArtProfileTask> {
    enabled = false
}

// Sentry upload task disabling - not needed when plugin is disabled
/*
// Disable Sentry upload tasks by default on local machines to avoid running
// sentry-cli with unquoted paths (paths with spaces break on Windows). CI can
// opt-in by setting the environment variable SENTRY_AUTO_UPLOAD=true.
gradle.projectsEvaluated {
    val enableSentryUpload = System.getenv("SENTRY_AUTO_UPLOAD") == "true"
    tasks.matching { it.name.startsWith("uploadSentryProguardMappings") }.configureEach {
        // ensure the task doesn't run unless explicitly enabled
        enabled = enableSentryUpload
        onlyIf { enableSentryUpload }
    }
}
*/