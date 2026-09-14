plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val cieApiBaseUrl = providers.gradleProperty("CIE_API_BASE_URL")
    .orElse("https://br-sparkling-dream-b1q8x3n5-cieapi.compute.c-5.eu-central-1.aws.neon.tech")
    .get()
val cieIdentityBaseUrl = providers.gradleProperty("CIE_IDENTITY_BASE_URL")
    .orElse("https://br-sparkling-dream-b1q8x3n5-cieidentity.compute.c-5.eu-central-1.aws.neon.tech")
    .get()

android {
    namespace = "com.cie.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cie.app.stable085"
        minSdk = 29
        targetSdk = 36
        versionCode = 23
        versionName = "0.8.5"
        buildConfigField("String", "CIE_API_BASE_URL", "\"$cieApiBaseUrl\"")
        buildConfigField("String", "CIE_IDENTITY_BASE_URL", "\"$cieIdentityBaseUrl\"")
    }

    signingConfigs {
        create("cieBeta") {
            storeFile = rootProject.file("cie-test-signing.jks")
            storePassword = "CIE-Test-084-Only"
            keyAlias = "cieTest"
            keyPassword = "CIE-Test-084-Only"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = false
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("cieBeta")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("cieBeta")
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
