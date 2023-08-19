@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.com.android.library.get().pluginId)
    id(libs.plugins.org.jetbrains.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.com.google.dagger.hilt.android.get().pluginId)
}

android {
    namespace = "com.banglalink.toffee.lib"
    compileSdk = 32
    
    defaultConfig {
        minSdk = 21
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    
    flavorDimensions += listOf("lib")
    productFlavors {
        create("mobile") {
            dimension = "lib"
            buildConfigField("int", "DEVICE_TYPE", "1")
        }
        create("tv") {
            dimension = "lib"
            buildConfigField("int", "DEVICE_TYPE", "4")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    // Kotlin
    implementation(libs.kotlin.coroutines)
    
    // Hilt
    implementation(libs.bundles.hilt)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.compiler.kapt)
    
    // Jetpack
    ksp(libs.room.kapt)
    implementation(libs.paging)
    implementation(libs.bundles.room)
    
    // Image
    implementation(libs.coil.core)
    implementation(libs.exifinterface)
    
    // Network
    implementation(libs.gson)
    implementation(libs.bundles.retrofit)
    
    // Google Services
    implementation(libs.google.api.client) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "com.google.code.findbugs")
        exclude(module = "support-annotations")
        exclude(group = "com.google.guava")
    }
    implementation(libs.google.http.client) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "com.google.code.findbugs")
        exclude(module = "support-annotations")
        exclude(group = "com.google.guava")
    }
    
    // Firebase
    implementation(libs.bundles.firebase)
    
    // Reporting
    implementation(libs.facebook.sdk)
    implementation(libs.bundles.mqtt)
    implementation(libs.pub.sub) {
        exclude(group = "com.google.code.findbugs")
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.json")
        exclude(module = "support-annotations")
        exclude(group = "com.google.guava")
    }
    
    
    /////// Testing
    
    testImplementation(libs.junit.core)
    
    kaptAndroidTest(libs.hilt.kapt.test)
    
    androidTestImplementation(libs.junit.ktx)
    androidTestImplementation(libs.hilt.android.test)
}