@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.internal.tasks.CompileArtProfileTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "com.maxrave.kotlinytmusicscraper"
        compileSdk = 37
        minSdk = 26
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "kotlinytmusicscraperKit"

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    jvm {
    }

    dependencies {
        implementation(platform(libs.koin.bom))
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies here
                implementation(projects.domain)
                implementation(projects.common)
                implementation(projects.ktorExt)
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.encoding)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.reflect)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.serialization.kotlinx.xml)
                implementation(libs.ktor.serialization.kotlinx.protobuf)

                implementation(libs.ksoup.html)
                implementation(libs.ksoup.entities)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation(libs.koin.android)
                implementation(libs.ffmpeg.kit.audio)
                implementation(libs.gson)

                implementation(libs.pipepipe.extractor)
                implementation(libs.brave.extractor)
                implementation(libs.okhttp3.okhttp)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.pipepipe.extractor)
                implementation(libs.brave.extractor)
                implementation(libs.okhttp3.okhttp)
            }
        }
    }
}

// PipePipe brings com.google.protobuf:protobuf-java (full) while Brave brings
// com.google.protobuf:protobuf-javalite. Both occupy the com.google.protobuf.* namespace and
// trigger DEX duplicate-class failures. Drop the full variant globally so Brave's javalite wins.
// nanojson force is in the root build.gradle.kts so it propagates to the final APK classpath.
configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
}

tasks.withType<CompileArtProfileTask> {
    enabled = false
}