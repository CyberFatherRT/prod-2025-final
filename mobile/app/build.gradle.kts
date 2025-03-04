plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.1.10"
}

android {
    namespace = "ru.prodcontest.booq"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.prodcontest.booq"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "PRODPROD"
            storeFile = file("$rootDir/app-keystore.jks")
            storePassword = "PRODPROD"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            signingConfig = signingConfigs["release"]
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.shimmer)
    implementation(libs.qrcode)
    // Dagger Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation)
    ksp(libs.hilt.compiler)

    // Ktor
    implementation(libs.ktor.core)
    implementation(libs.ktor.android)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.contentnegotiation)
    implementation(libs.kotlinx.serialization.json)

    // Navigaton
    implementation(libs.navigation)

    // Koil
    implementation(libs.coil.compose)
    implementation(libs.coil.ktor)

    implementation(libs.datastore)

    implementation(libs.constraintLayout)

    implementation(libs.accompanist.permissions)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.mlkit.barcode)
    implementation(libs.camera.vision)
    implementation(libs.qrkit)
}