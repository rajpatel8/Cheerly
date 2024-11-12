plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.rajkumar.cheerly"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rajkumar.cheerly"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "cheerly"
        manifestPlaceholders["appAuthRedirectUri"] = "cheerly"
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

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/INDEX.LIST",
                "META-INF/*.kotlin_module",
                "META-INF/MANIFEST.MF",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Location
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")

    // For OpenStreetMap API
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // YouTube & Google APIs
    implementation("com.google.apis:google-api-services-youtube:v3-rev20231011-2.0.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")

    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Network & JSON
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // Use okhttp

    // JSON parsing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Google Services & Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation("com.google.android.gms:play-services-base:18.3.0")

    // YouTube API
    implementation("com.google.apis:google-api-services-youtube:v3-rev20231011-2.0.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")

    // OAuth
    implementation("net.openid:appauth:0.11.1")
    implementation(libs.androidx.browser)

    // Network
    implementation(libs.retrofit)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // UI Components
    implementation(libs.material)
    implementation("com.google.android.material:material:1.10.0")

    // Image Loading
    implementation("io.coil-kt:coil:2.4.0")

    // Firebase
    implementation(libs.firebase.crashlytics)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation(libs.androidx.gridlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}