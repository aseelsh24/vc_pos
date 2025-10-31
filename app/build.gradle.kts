// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.29" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
}
android {
    namespace = "com.aseel.pos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aseel.pos"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
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
        kotlinCompilerExtensionVersion = "1.5.14"  // FIXED: Was 1.5.15
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")  // FIXED: Was 1.15.0
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")  // FIXED: Was 2.8.7
    implementation("androidx.activity:activity-compose:1.9.2")  // FIXED: Was 1.9.3

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")  // FIXED: Was 2024.11.00
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.1")  // FIXED: Was 2.8.4

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")  // FIXED: Was 2.52
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")  // FIXED: Was 2.52
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")  // FIXED: Was 1.7.3

    // WindowSizeClass for adaptive layouts
    implementation("androidx.compose.material3:material3-window-size-class")

    // Barcode scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.camera:camera-camera2:1.4.0-beta03")  // FIXED: Was 1.4.0-alpha11
    implementation("androidx.camera:camera-lifecycle:1.4.0-beta03")  // FIXED: Was 1.4.0-alpha11
    implementation("androidx.camera:camera-view:1.4.0-beta03")  // FIXED: Was 1.4.0-alpha11

    // ESC/POS Thermal Printer
    implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("1.0.1")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("1.0.1")
    }
}
