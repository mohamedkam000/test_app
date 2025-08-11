plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(19)
}

android {
    namespace = "com.test.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.test.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("test.p12")
            storePassword = "0000"
            keyAlias = "Test"
            keyPassword = "0000"
            storeType = "pkcs12"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.15"
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.07.00"))
    implementation("androidx.compose.ui:ui:1.9.0-rc01")
    implementation("androidx.compose.animation:animation:1.9.0-rc01")
    implementation("androidx.compose.ui:ui-tooling:1.9.0-rc01")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.0-rc01")
    implementation("androidx.activity:activity-compose:1.12.0-alpha05")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.runtime:runtime:1.9.0-rc01")
    implementation("androidx.compose.material3:material3:1.5.0-alpha01")
    implementation("androidx.navigation:navigation-compose:2.9.2")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0-alpha03")
    implementation("androidx.activity:activity-ktx:1.12.0-alpha05")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0-alpha01")
    implementation("androidx.compose.material3:material3-window-size-class:1.5.0-alpha01")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
//    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
//    implementation("com.airbnb.android:lottie-compose:6.6.7")
//    implementation("com.github.spotbugs:spotbugs-annotations:4.9.3")
}