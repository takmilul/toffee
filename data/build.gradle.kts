@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    with(libs.plugins) {
        id(com.android.library.get().pluginId)
        id(org.jetbrains.kotlin.android.get().pluginId)
        id(kotlin.parcelize.get().pluginId)
        id(ksp.get().pluginId)
        id(com.google.dagger.hilt.android.get().pluginId)
    }
}

android {
    namespace = "com.banglalink.toffee.lib"
    compileSdk = 33
    
    val properties = Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "secret.properties")))
    }
    val libraryPackageName: String = properties.getProperty("libraryPackageName")
    
    defaultConfig {
        minSdk = 21
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        ndk {
            // Specifies the ABI configurations of your native
            // libraries Gradle should build and package with your app.
            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    flavorDimensions += listOf("lib")
    productFlavors {
        create("mobile") {
            dimension = "lib"
            buildConfigField("int", "DEVICE_TYPE", "1")
            buildConfigField("String", "LIBRARY_PACKAGE", libraryPackageName)
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
            isJniDebuggable = false
            isRenderscriptDebuggable = false
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
    with(libs) {
        // Kotlin
        implementation(kotlin.coroutines)
        
        // Hilt
        implementation(bundles.hilt)
        ksp(hilt.compiler)
        ksp(hilt.compiler.kapt)
        
        // Jetpack
        ksp(room.kapt)
        implementation(paging)
        implementation(bundles.room)
        
        // Image
        implementation(coil.core)
        implementation(exifinterface)
        
        // Network
        implementation(gson)
        implementation(bundles.retrofit)
        
        // Google Services
        implementation(google.api.client) {
            exclude(group = "org.apache.httpcomponents", module = "httpclient")
            exclude(group = "com.google.code.findbugs")
            exclude(module = "support-annotations")
            exclude(group = "com.google.guava")
        }
        implementation(google.http.client) {
            exclude(group = "org.apache.httpcomponents", module = "httpclient")
            exclude(group = "com.google.code.findbugs")
            exclude(module = "support-annotations")
            exclude(group = "com.google.guava")
        }
        
        // Firebase
        implementation(bundles.firebase)
        
        // Reporting
        implementation(facebook.sdk)
        implementation(bundles.mqtt)
        implementation(pub.sub) {
            exclude(group = "com.google.code.findbugs")
            exclude(group = "org.apache.httpcomponents", module = "httpclient")
            exclude(group = "org.json")
            exclude(module = "support-annotations")
            exclude(group = "com.google.guava")
        }
        
        
        /////// Testing
        testImplementation(junit.core)
        
        kspAndroidTest(hilt.kapt.test)
        
        androidTestImplementation(junit.ktx)
        androidTestImplementation(hilt.android.test)
    }
}