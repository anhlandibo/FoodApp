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
        versionCode = 2
        versionName = "1.1"

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
    }

    packagingOptions {
        // Chỉ định Gradle chỉ lấy một trong các file trùng lặp.
        // pickFirst sẽ chọn file đầu tiên nó tìm thấy.
        pickFirst("META-INF/DEPENDENCIES")
        pickFirst("META-INF/LICENSE")
        pickFirst("META-INF/LICENSE.txt")
        pickFirst("META-INF/NOTICE")
        pickFirst("META-INF/NOTICE.txt")
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
    implementation ("com.google.android.gms:play-services-auth:20.7.0") // Explicit version for Play Services

    // Other libraries
    implementation(libs.runner)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation components
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // UI components and additional libraries
    implementation("com.github.bumptech.glide:glide:4.16.0") // Avoid duplicating Glide versions
    implementation(libs.firebase.storage)
    implementation(libs.play.services.maps)
    implementation(libs.core.animation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("com.google.android.material:material:1.13.0-alpha11")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("com.google.firebase:firebase-auth:21.0.3")
    implementation ("com.google.firebase:firebase-firestore:24.8.1")
    // Unit and UI tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    //OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Load img from url
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    // Circle img view
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // Toast
    implementation("com.github.GrenderG:Toasty:1.5.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.7.0")

    // Cloudinary HTTP5 client (Thường dùng cho backend Java, không phải Android client)
    implementation("com.cloudinary:cloudinary-http5:2.0.0")

    // Cloudinary Taglib (Thường dùng cho ứng dụng web Java/JSP, không phải Android)
    implementation("com.cloudinary:cloudinary-taglib:2.0.0")

    // Dotenv Java (Thường dùng cho backend Java để quản lý biến môi trường, không phải Android client)
    implementation("io.github.cdimascio:dotenv-java:2.2.4")

    implementation("com.cloudinary:cloudinary-android:3.0.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

//    // Spring Boot Starter Web
//    implementation("org.springframework.boot:spring-boot-starter-web")
//
//    // Spring Boot Starter Data JPA
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//
//    // Spring Boot Starter Security
//    implementation("org.springframework.boot:spring-boot-starter-security")
//
//    // Spring Boot Starter Thymeleaf
//    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
}