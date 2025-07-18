@file:Suppress("UnstableApiUsage")

plugins {
    with(libs.plugins) {
        alias(ksp)
        alias(android.library)
        alias(kotlin.android)
        alias(kotlin.parcelize)
        alias(kotlin.serialize)
        alias(hilt.android)
    }
}

android {
    namespace = "com.banglalink.toffee.lib"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    
    val appVersionCode: String = libs.versions.appVersionCode.get()
    val appVersionName: String = libs.versions.appVersionName.get()
    
    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
//        externalNativeBuild {
//            cmake {
//                // Passes optional arguments to CMake.
//                arguments += listOf("-DANDROID_ARM_NEON=TRUE", "-DANDROID_TOOLCHAIN=clang")
//                // Sets a flag to enable format macro constants for the C compiler.
//                cFlags += listOf("-D__STDC_FORMAT_MACROS")
//                // Sets optional flags for the C++ compiler.
//                cppFlags += listOf("-fexceptions", "-frtti")
//            }
//        }
//        Similar to other properties in the defaultConfig block,
//        you can configure the ndk block for each product flavor
//        in your build configuration.
//        ndk {
////            Specifies the ABI configurations of your native
////            libraries Gradle should build and package with your app.
//            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
//        }
//        These build configs will be user for all productFlavors or buildTypes. You can override them in any specific 
//        productFlavor or buildType block below.
        buildConfigField("int", "DEVICE_TYPE", "1")
        buildConfigField("int", "APP_VERSION_CODE", appVersionCode)
        buildConfigField("String", "APP_VERSION_NAME", "\"$appVersionName\"")
    }
    
    flavorDimensions += listOf("lib")
    
    productFlavors {
        create("mobile") {
            dimension = "lib"
        }
        create("tv") {
            dimension = "lib"
            buildConfigField("int", "DEVICE_TYPE", "4")
        }
    }
    
    buildTypes {
        getByName("debug") {
            isJniDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
//            ndk {
////            debugSymbolLevel = "FULL"
////            Specifies the ABI configurations of your native
////            libraries Gradle should build and package with your app.
//                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
//            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = false
            isJniDebuggable = false
//            ndk {
////            debugSymbolLevel = "FULL"
////            Specifies the ABI configurations of your native
////            libraries Gradle should build and package with your app.
//                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
//            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("stagingDebug") {
            initWith(getByName("debug"))
        }
        create("stagingRelease") {
            initWith(getByName("release"))
        }
        create("productionDebug") {
            initWith(getByName("debug"))
        }
        create("productionRelease") {
            initWith(getByName("release"))
        }
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = false
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
    
//    ndkVersion = "26.2.11394342"
    
//    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//            version = "3.22.1"
//        }
//    }
    
//    sourceSets {
//        getByName("main") {
//            jniLibs.srcDir("src/main/libs")
//        }
//    }
    
    buildFeatures {
        buildConfig = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    with(libs) {
        // Kotlin
        coreLibraryDesugaring(desugar)
        implementation(kotlin.coroutines)
        implementation(kotlin.json.serialization)
        implementation(kotlin.json.converter.factory)
        
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
//        implementation(gson)
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
        debugImplementation(chucker.dev)  // dev
        releaseImplementation(chucker.prod) // live
        
        
        /////// Testing
        testImplementation(junit.core)
        
        kspAndroidTest(hilt.kapt.test)
        
        androidTestImplementation(junit.ktx)
        androidTestImplementation(hilt.android.test)
    }
}