//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.google.gms.google.services)
//    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
//}
//
//android {
//    namespace = "com.example.foodapp2025"
//    compileSdk = 35
//
//    defaultConfig {
//        applicationId = "com.example.foodapp2025"
//        minSdk = 33
//        targetSdk = 35
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//
//    buildFeatures {
//        viewBinding = true
//    }
//}
//
//dependencies {
//    implementation(libs.appcompat)
//    implementation(libs.material)
//    implementation(libs.activity)
//    implementation(libs.constraintlayout)
//
//    // Firebase libraries
//    implementation(libs.firebase.firestore)
//    implementation(libs.firebase.auth)
//
//    // Google Sign-In libraries
//    implementation(libs.credentials)
//    implementation(libs.credentials.play.services.auth)
//    implementation(libs.googleid)
//    implementation ("com.google.android.gms:play-services-auth:20.7.0") // Explicit version for Play Services
//
//    // Other libraries
//    implementation(libs.runner)
//    implementation(libs.legacy.support.v4)
//    implementation(libs.lifecycle.livedata.ktx)
//    implementation(libs.lifecycle.viewmodel.ktx)
//
//    // Navigation components
//    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
//    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
//
//    // UI components and additional libraries
//    implementation("com.github.bumptech.glide:glide:4.16.0") // Avoid duplicating Glide versions
//    implementation(libs.firebase.storage)
//    implementation(libs.play.services.maps)
//    implementation(libs.core.animation)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.ext.junit)
//    androidTestImplementation(libs.espresso.core)
//    implementation("com.github.bumptech.glide:glide:4.12.0")
//    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
//    implementation("com.google.code.gson:gson:2.9.1")
//    implementation("com.google.android.material:material:1.13.0-alpha11")
//    implementation ("com.google.android.gms:play-services-auth:21.3.0")
//    implementation ("com.google.firebase:firebase-auth:21.0.3")
//    implementation ("com.google.firebase:firebase-firestore:24.8.1")
//
//
//    // Unit and UI tests
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.ext.junit)
//    androidTestImplementation(libs.espresso.core)
//    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
//    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
//
//    //OpenStreetMap
//    implementation("org.osmdroid:osmdroid-android:6.1.14")
//    implementation("com.google.android.gms:play-services-location:21.0.1")
//
//    // Load img from url
//    implementation ("com.github.bumptech.glide:glide:4.16.0")
//    // Circle img view
//    implementation ("de.hdodenhof:circleimageview:3.1.0")
//
//    // Toast
//    implementation("com.github.GrenderG:Toasty:1.5.2")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//
//    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
//    implementation("com.google.firebase:firebase-messaging")
//
//
//    // Stripe Android SDK
//    implementation("com.stripe:stripe-android:20.37.0")
//
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
//
//    // Compose BOM (Bill of Materials) to manage Compose versions
//
//    // Core Compose libraries
//    implementation("androidx.compose.ui:ui")
//    implementation("androidx.compose.ui:ui-graphics")
//    implementation("androidx.compose.ui:ui-tooling-preview")
//    implementation("androidx.compose.material:material") // Or" material3 for newer designs
//    implementation("androidx.compose.material:material-icons-core")
//    implementation("androidx.compose.material:material-icons-extended")
//    implementation(platform("androidx.compose:compose-bom:2023.08.00")) // Check for the latest stable version
//
//    // Activity integration for Compose
//    implementation("androidx.activity:activity-compose:1.8.2") // Check for the latest stable version
//
//    // Runtime specific for Compose
//    implementation("androidx.compose.runtime:runtime-livedata") // If using LiveData with Compose
//
//    // UI Tooling (for previewing in Android Studio)
//    debugImplementation("androidx.compose.ui:ui-tooling")
//    debugImplementation("androidx.compose.ui:ui-test-manifest")
//}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.foodapp2025"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodapp2025"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        compose = true // Bật Compose
    }

    // THÊM PHẦN NÀY - Quan trọng cho Compose
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase libraries
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Google Sign-In libraries
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Other libraries
    implementation(libs.runner)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation components
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // UI components and additional libraries
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.storage)
    implementation(libs.play.services.maps)
    implementation(libs.core.animation)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // UI Libraries
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.google.android.material:material:1.13.0-alpha11")
    implementation("com.google.firebase:firebase-auth:21.0.3")
    implementation("com.google.firebase:firebase-firestore:24.8.1")

    //OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Image loading
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Toast
    implementation("com.github.GrenderG:Toasty:1.5.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-messaging")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // ===== COMPOSE DEPENDENCIES - CẬP NHẬT =====
    // Compose BOM - Phiên bản mới nhất
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    // Core Compose libraries
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3") // THÊM Material3
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // THÊM CÁC DEPENDENCY QUAN TRỌNG
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-util")

    // Activity integration for Compose
    implementation("androidx.activity:activity-compose")

    // Runtime specific for Compose
    implementation("androidx.compose.runtime:runtime-livedata")

    // UI Tooling (for previewing in Android Studio)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ===== STRIPE - CẬP NHẬT PHIÊN BẢN =====
    implementation("com.stripe:stripe-android:20.48.0") // Phiên bản mới nhất tương thích
}