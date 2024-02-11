@file:Suppress("UnstableApiUsage")

plugins {
    with(libs.plugins) {
        id(android.library.get().pluginId)
        id(kotlin.android.get().pluginId)
        id(kotlin.parcelize.get().pluginId)
        id(kotlin.serialize.get().pluginId) version libs.versions.kotlin.version.get()
        id(ksp.get().pluginId)
        id(hilt.android.get().pluginId)
    }
}

android {
    namespace = "com.banglalink.toffee.lib"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                // Passes optional arguments to CMake.
                arguments += listOf("-DANDROID_ARM_NEON=TRUE", "-DANDROID_TOOLCHAIN=clang")
                // Sets a flag to enable format macro constants for the C compiler.
                cFlags += listOf("-D__STDC_FORMAT_MACROS")
                // Sets optional flags for the C++ compiler.
                cppFlags += listOf("-fexceptions", "-frtti")
            }
        }
//        Similar to other properties in the defaultConfig block,
//        you can configure the ndk block for each product flavor
//        in your build configuration.
        ndk {
//            Specifies the ABI configurations of your native
//            libraries Gradle should build and package with your app.
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/libs")
        }
    }
    
    flavorDimensions += listOf("lib")
    productFlavors {
        create("mobile") {
            dimension = "lib"
            
            val appVersionCode: String = libs.versions.appVersionCode.get()
            val appVersionName: String = libs.versions.appVersionName.get()
            
            buildConfigField("int", "DEVICE_TYPE", "1")
            buildConfigField("int", "APP_VERSION_CODE", appVersionCode)
            buildConfigField("String", "APP_VERSION_NAME", "\"$appVersionName\"")
        }
        create("tv") {
            dimension = "lib"
            
            val appVersionCode: String = libs.versions.appVersionCode.get()
            val appVersionName: String = libs.versions.appVersionName.get()
            
            buildConfigField("int", "DEVICE_TYPE", "4")
            buildConfigField("int", "APP_VERSION_CODE", appVersionCode)
            buildConfigField("String", "APP_VERSION_NAME", "\"$appVersionName\"")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isJniDebuggable = false
            ndk {
//            debugSymbolLevel = "FULL"
//            Specifies the ABI configurations of your native
//            libraries Gradle should build and package with your app.
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isJniDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            ndk {
//            debugSymbolLevel = "FULL"
//            Specifies the ABI configurations of your native
//            libraries Gradle should build and package with your app.
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = true
//            pickFirsts += listOf(
//                "lib/*/libnative-lib.so"
//            )
        }
        resources {
//            pickFirsts += listOf(
//                "lib/*/libnative-lib.so"
//            )
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
        ksp(hilt.android.compiler)
        
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
        implementation(okhttp.pretty.logger) {
            exclude(group = "org.json", module = "json")
        }
        
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
        
        // Logging
        implementation(bundles.logger)
        
        
        /////// Testing
        testImplementation(junit.core)
        
        kspAndroidTest(hilt.kapt.test)
        
        androidTestImplementation(junit.ktx)
        androidTestImplementation(hilt.android.test)
    }
}