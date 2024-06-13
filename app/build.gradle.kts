import java.io.FileInputStream
import java.util.Properties

plugins {
    with(libs.plugins) {
        alias(ksp)
        alias(kotlin.kapt)
        alias(android.application)
        alias(kotlin.android)
        alias(kotlin.parcelize)
        alias(kotlin.serialize)
        alias(google.services)
        alias(navigation.safeargs)
        alias(hilt.android)
        alias(firebase.crashlytics)
        alias(firebase.appdistribution)
        alias(play.publisher)
        id(conviva.tracker.plugin.get().pluginId)
    }
}

android {
    namespace = "com.banglalink.toffee"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    
    val properties = Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "secret.properties")))
    }
    val prodServerUrl: String = properties.getProperty("prodServerUrl")
    val stagingServerUrl: String = properties.getProperty("stagingServerUrl")
    val adsAppId: String = properties.getProperty("adsAppId")
    val facebookAppId: String = properties.getProperty("facebookAppId")
    val medalliaApiKey: String = properties.getProperty("medalliaApiKey")
    val fireworkOAuthId: String = properties.getProperty("fireworkOAuthId")
    val convivaGatewayUrl: String = properties.getProperty("convivaGatewayUrl")
    val facebookClientToken: String = properties.getProperty("facebookClientToken")
    val convivaCustomerKeyTest: String = properties.getProperty("convivaCustomerKey-test")
    val convivaCustomerKeyProd: String = properties.getProperty("convivaCustomerKey-prod")
    
    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
        applicationId = "com.banglalink.toffee"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "com.banglalink.toffee.HiltTestRunner"
        ndk {
//            debugSymbolLevel = "FULL"
//            Specifies the ABI configurations of your native
//            libraries Gradle should build and package with your app.
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
        manifestPlaceholders.putAll(
            mapOf(
                "fireworkOAuthId" to fireworkOAuthId,
                "facebookAppId" to facebookAppId,
                "facebookClientToken" to facebookClientToken,
                "adsAppId" to adsAppId,
            )
        )
        configurations.all {
            resolutionStrategy {
                force("androidx.emoji2:emoji2-views-helper:1.3.0")
                force("androidx.emoji2:emoji2:1.3.0")
            }
        }
//        Use Staging Server by default. For Production build, override BASE_URL BuildConfig in the specific buildTypes block below.
        buildConfigField("int", "DEVICE_TYPE", "1")
//        buildConfigField("String", "BASE_URL", prodServerUrl)
        buildConfigField("String", "BASE_URL", stagingServerUrl)
        buildConfigField("String", "MEDALLIA_API_KEY", medalliaApiKey)
        buildConfigField("String", "FIREWORK_OAUTH_ID", fireworkOAuthId)
        buildConfigField("String", "CONVIVA_GATEWAY_URL", convivaGatewayUrl)
        buildConfigField("String", "CONVIVA_CUSTOMER_KEY_TEST", convivaCustomerKeyTest)
        buildConfigField("String", "CONVIVA_CUSTOMER_KEY_PROD", convivaCustomerKeyProd)
    }
    
    signingConfigs {
        create("config") {
            if (project.hasProperty("TOFFEE_KEYSTORE_FILE")) {
                storeFile = file(project.findProperty("TOFFEE_KEYSTORE_FILE").toString())
                storePassword = project.findProperty("TOFFEE_KEYSTORE_PASSWORD")?.toString()
                keyAlias = project.findProperty("TOFFEE_KEY_ALIAS")?.toString()
                keyPassword = project.findProperty("TOFFEE_KEY_PASSWORD")?.toString()
            }
        }
    }
    
    flavorDimensions += listOf("lib")
    
    productFlavors {
        create("mobile") {
            dimension = "lib"
        }
    }
    
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isJniDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            ndk {
//            debugSymbolLevel = "FULL"
//            Specifies the ABI configurations of your native
//            libraries Gradle should build and package with your app.
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            }
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = "distribution/whatsnew/whatsnew-en-US"  // ignore this if releaseNotes is being used
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
//            debugSymbolLevel = "FULL"
//            Specifies the ABI configurations of your native
//            libraries Gradle should build and package with your app.
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            }
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "ND-QA, BL-UAT"
                releaseNotesFile = "distribution/whatsnew/whatsnew-en-US"  // ignore this if releaseNotes is being used
            }
            if (project.hasProperty("TOFFEE_KEYSTORE_FILE")) {
                signingConfig = signingConfigs.getByName("config")
            }
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
            buildConfigField("String", "BASE_URL", prodServerUrl)
        }
        create("productionRelease") {
            initWith(getByName("release"))
            buildConfigField("String", "BASE_URL", prodServerUrl)
        }
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = false
//            pickFirsts += listOf(
//                "lib/*/libnative-lib.so"
//            )
            excludes += listOf(
                "lib/*/librsjni.so",
                "lib/*/libRSSupport.so",
                "lib/*/librsjni_androidx.so"
            )
        }
        resources {
            excludes += listOf("META-INF/*.kotlin_module")
            pickFirsts += listOf(
//                "lib/*/libnative-lib.so",
                "META-INF/services",
                "META-INF/LICENSE",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/annotation-experimental_release.kotlin_module"
            )
        }
    }
    
    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/libs")
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.compose.version.get()
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar", "*.so"))))
    implementation(project(":data"))
    implementation(project(":balloon"))
    
    with (libs) {
        // View
        implementation(activity)
        implementation(fragment.ktx)
        implementation(splashscreen)
        implementation(material)
        implementation(cardview)
        implementation(switch.button)
        coreLibraryDesugaring(desugar)
        implementation(circleimageview)
        implementation(constraint.layout)
        implementation(legacy.support.v4)
        
        // Kotlin
        implementation(kotlin.stdlib)
        implementation(core.ktx)
        implementation(kotlin.coroutines)
        implementation(kotlin.json.serialization)
        
        // Hilt
        implementation(bundles.hilt)
        ksp(hilt.compiler)
        ksp(hilt.compiler.kapt)
        ksp(hilt.android.compiler)
        
        // Jetpack
        ksp(room.kapt)
        implementation(paging)
        implementation(bundles.room)
        implementation(platform(compose.bom))
        implementation(bundles.compose)
        implementation(work.manager.ktx)
        implementation(bundles.lifecycle)
        implementation(bundles.navigation)
        
        // Image
//        implementation(lottie)
        implementation(bundles.coil)
        implementation(bundles.image.crop)
        
        // Player
//    implementation(bundles.exoplayer)
        implementation(bundles.media3.player)
        implementation(bundles.cast)
        implementation(bundles.ads)
        
        // Security
//    implementation(bundles.security)
        
        // Network
        implementation(bundles.retrofit)
        implementation(net.gotev.uploadservice)
        implementation(net.gotev.uploadservice.okhttp)
        
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
        
        // Play Services
        implementation(bundles.play.services)
        
        // Firebase
        implementation(bundles.firebase)
        
        // Firework
        implementation(firework.ads)
        implementation(firework.sdk) {
            exclude(module = "picasso-transformations")
        }
        
        // Reporting
        implementation(bundles.conviva)
        implementation(bundles.mqtt)
        implementation(pub.sub) {
            exclude(group = "com.google.code.findbugs")
            exclude(group = "org.apache.httpcomponents")
            exclude(group = "org.json")
            exclude(module = "support-annotations")
            exclude(group = "com.google.guava")
        }
        
        // Logging
        implementation(bundles.logger)
        
        // Miscellaneous
        implementation(butterknife)
        implementation(shimmer)
//        implementation(medallia)
//        implementation(clarity)
        implementation(guava)
        
        
        /////// Testing
        kspTest(hilt.kapt.test)
        kspAndroidTest(hilt.kapt.test)
        
        testImplementation(junit.core)
        testImplementation(robolectric)
        testImplementation(mockk.core)
        testImplementation(mockito.kotlin)
        testImplementation(coroutines.test)
        testImplementation(hilt.android.test)
        testImplementation(okhttp.mock.web.server)
        
        androidTestImplementation(junit.ktx)
        androidTestImplementation(test.runner)
        androidTestImplementation(test.rules)
        androidTestImplementation(test.truth)
        androidTestImplementation(test.core.ktx)
        androidTestImplementation(google.truth)
        androidTestImplementation(mockk.android)
        androidTestImplementation(bundles.espresso)
        androidTestImplementation(hilt.android.test)
        
        debugImplementation(fragment.test)
//        debugImplementation (leakcanary)
    }
}