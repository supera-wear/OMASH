plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val cieApiBaseUrl = providers.gradleProperty("CIE_API_BASE_URL").orElse("https://example.invalid").get()
val cieMessageApiBaseUrl = providers.gradleProperty("CIE_MESSAGE_API_BASE_URL").orElse(cieApiBaseUrl).get()

android {
    namespace = "com.cie.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cie.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 13
        versionName = "0.9.3"
        buildConfigField("String", "CIE_API_BASE_URL", "\"$cieApiBaseUrl\"")
        buildConfigField("String", "CIE_MESSAGE_API_BASE_URL", "\"$cieMessageApiBaseUrl\"")
        buildConfigField("boolean", "CIE_COMMUNITY_LEARNING_ENABLED", "false")
        buildConfigField("boolean", "CIE_RAW_MESSAGE_UPLOAD_ENABLED", "false")
        buildConfigField("boolean", "CIE_MMS_READY", "false")
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
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
