plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val cieApiBaseUrl = providers.gradleProperty("CIE_API_BASE_URL")
    .orElse("https://br-sparkling-dream-b1q8x3n5-cieapi.compute.c-5.eu-central-1.aws.neon.tech")
    .get()

android {
    namespace = "com.cie.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cie.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 9
        versionName = "0.7.1"
        buildConfigField("String", "CIE_API_BASE_URL", "\"$cieApiBaseUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
