plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.2.20-2.0.2"
}

android {
    namespace = "com.example.smartnotifier"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.teyanday.smartnotifier"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    val room_version = "2.8.2"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.dagger:dagger:2.57.2")
    ksp("com.google.dagger:dagger-compiler:2.57.2")
    implementation("androidx.room:room-runtime:${room_version}")
    implementation("androidx.room:room-ktx:${room_version}")
    ksp("androidx.room:room-compiler:${room_version}")
// ViewModelのライフサイクル拡張機能 (viewModelScopeを提供)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
// (FragmentやActivityで lifecycleScope を使うために、これも必要かもしれません)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
// コルーチン自体のライブラリ (通常は自動で含まれるが念のため)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}