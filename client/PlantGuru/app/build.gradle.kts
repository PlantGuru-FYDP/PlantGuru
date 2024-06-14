plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.plantguru"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.plantguru"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue ("string", "proof_of_possesion", "abcd1234")
            resValue ("string", "wifi_base_url", "192.168.4.1:80")
            resValue ("string", "wifi_device_name_prefix", "GURU")
            resValue ("string", "ble_device_name_prefix", "GURU")
            buildConfigField ("boolean", "isQrCodeSupported", "false")
            buildConfigField ("boolean", "isSettingsAllowed", "true")
            buildConfigField ("boolean", "isFilteringByPrefixAllowed", "true")
        }
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue ("string", "proof_of_possesion", "abcd1234")
            resValue ("string", "wifi_base_url", "192.168.4.1:80")
            resValue ("string", "wifi_device_name_prefix", "GURU")
            resValue ("string", "ble_device_name_prefix", "GURU")
            buildConfigField ("boolean", "isQrCodeSupported", "false")
            buildConfigField ("boolean", "isSettingsAllowed", "true")
            buildConfigField ("boolean", "isFilteringByPrefixAllowed", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.espressif:esp-idf-provisioning-android:lib-2.1.4")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.1.0")
    implementation("androidx.preference:preference:1.1.1")

    implementation("com.google.protobuf:protobuf-javalite:3.18.0")
    implementation("com.google.crypto.tink:tink-android:1.6.1")

    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.github.yuriy-budiyev:code-scanner:2.1.2")
    implementation("com.github.firdausmaulan:AVLoadingIndicatorView:2.3.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")

    implementation("androidx.navigation:navigation-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}