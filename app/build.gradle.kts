import java.io.File

// ┌──────────────────────────────────────────────────────┐
// │  SHAHRIAR VPN — app module                           │
// │  Cyberpunk Xray client for Android                   │
// └──────────────────────────────────────────────────────┘

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.shahriar.vpn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shahriar.vpn"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// The Xray core AAR (libv2ray.aar) is ~65MB, so it is NOT stored in git.
// It is downloaded automatically before every build.
val xrayAar = File(projectDir, "libs/libv2ray.aar")
val xrayAarUrl = "https://github.com/2dust/AndroidLibXrayLite/releases/latest/download/libv2ray.aar"

tasks.register("downloadXrayAar") {
    description = "Downloads the Xray core Android library (libv2ray.aar)"
    outputs.file(xrayAar)
    doLast {
        if (!xrayAar.exists()) {
            xrayAar.parentFile.mkdirs()
            logger.lifecycle(">>> Downloading Xray core AAR (~65 MB), please wait ...")
            val isWindows = System.getProperty("os.name", "").lowercase().contains("win")
            if (isWindows) {
                exec {
                    commandLine(
                        "powershell", "-NoProfile", "-Command",
                        "[Net.ServicePointManager]::SecurityProtocol='Tls12'; " +
                        "Invoke-WebRequest -Uri '$xrayAarUrl' -OutFile '${xrayAar.absolutePath}'"
                    )
                }
            } else {
                exec {
                    commandLine("curl", "-sL", "--fail", "-o", xrayAar.absolutePath, xrayAarUrl)
                }
            }
            logger.lifecycle(">>> Xray AAR downloaded: ${xrayAar.length()} bytes")
        }
    }
}

tasks.named("preBuild") { dependsOn("downloadXrayAar") }

dependencies {
    // Xray core (go mobile bindings) — auto-downloaded by the task above
    implementation(files("libs/libv2ray.aar"))

    // AndroidX core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
