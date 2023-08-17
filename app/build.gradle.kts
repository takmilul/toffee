@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.com.android.application.get().pluginId)
    id(libs.plugins.org.jetbrains.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.com.google.dagger.hilt.android.get().pluginId)
    id(libs.plugins.androidx.navigation.safeargs.get().pluginId)
    id(libs.plugins.com.gms.google.services.get().pluginId)
    id(libs.plugins.com.google.firebase.crashlytics.get().pluginId)
}

android {
    namespace = "com.banglalink.toffee"
    compileSdk = 33
    
    val properties = Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "secret.properties")))
    }
    val fireworkOAuthId: String = properties.getProperty("fireworkOAuthId")
    val facebookAppId: String = properties.getProperty("facebookAppId")
    val facebookClientToken: String = properties.getProperty("facebookClientToken")
    val adsAppId: String = properties.getProperty("adsAppId")
    val medalliaApiKey: String = properties.getProperty("medalliaApiKey")
    val convivaCustomerKeyTest: String = properties.getProperty("convivaCustomerKey-test")
    val convivaCustomerKeyProd: String = properties.getProperty("convivaCustomerKey-prod")
    val convivaGatewayUrl: String = properties.getProperty("convivaGatewayUrl")
//    val fireworkOAuthId: String = gradleLocalProperties(rootDir).getProperty("firework.oAuthId")
    
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        versionCode = 111
        versionName = "5.0.0"
        applicationId = "com.banglalink.toffee"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "com.banglalink.toffee.HiltTestRunner"
        ndk {
            debugSymbolLevel = "FULL"
        }
        manifestPlaceholders.putAll(
            mapOf(
                "fireworkOAuthId" to fireworkOAuthId,
                "facebookAppId" to facebookAppId,
                "facebookClientToken" to facebookClientToken,
                "adsAppId" to adsAppId,
            )
        )
    }
    
    flavorDimensions += listOf("lib")
    
    productFlavors {
        create("mobile") {
            dimension = "lib"
            buildConfigField("int", "DEVICE_TYPE", "1")
            buildConfigField("String", "MEDALLIA_API_KEY", medalliaApiKey)
            buildConfigField("String", "FIREWORK_OAUTH_ID", fireworkOAuthId)
            buildConfigField("String", "CONVIVA_GATEWAY_URL", convivaGatewayUrl)
            buildConfigField("String", "CONVIVA_CUSTOMER_KEY_TEST", convivaCustomerKeyTest)
            buildConfigField("String", "CONVIVA_CUSTOMER_KEY_PROD", convivaCustomerKeyProd)
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
//        useIR = true
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = false
            excludes += listOf(
                "lib/*/librsjni.so",
                "lib/*/libRSSupport.so",
                "lib/*/librsjni_androidx.so"
            )
        }
        resources {
            excludes += listOf("META-INF/*.kotlin_module")
            pickFirsts += listOf(
                "META-INF/services",
                "META-INF/LICENSE",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "META-INF/annotation-experimental_release.kotlin_module"
            )
        }
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(project(":data"))
    
    // View
    implementation(libs.activity)
    implementation(libs.fragment.ktx)
    implementation(libs.splashscreen)
    implementation(libs.material)
    implementation(libs.cardview)
    implementation(libs.switch.button)
    implementation(libs.circleimageview)
    implementation(libs.constraint.layout)
    implementation(libs.legacy.support.v4)
    
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.core.ktx)
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
    implementation(libs.bundles.compose)
    implementation(libs.work.manager.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    
    // Image
    implementation(libs.lottie)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.image.crop)
    
    // Player
//    implementation(libs.bundles.exoplayer)
    implementation(libs.bundles.media3.player)
    implementation(libs.bundles.cast)
    implementation(libs.bundles.ads)
    
    // Security
//    implementation(libs.bundles.security)
    
    // Network
    implementation(libs.bundles.retrofit)
    implementation(libs.net.gotev.uploadservice)
    implementation(libs.net.gotev.uploadservice.okhttp)
    
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
    
    // Play Services
    implementation(libs.bundles.play.services)
    
    // Firebase
    implementation(libs.bundles.firebase)
    
    // Firework
    implementation(libs.firework.ads)
    implementation(libs.firework.sdk) {
        exclude(module = "picasso-transformations")
    }
    
    // Reporting
    implementation(libs.bundles.conviva)
    implementation(libs.bundles.mqtt)
    implementation(libs.pub.sub) {
        exclude(group = "com.google.code.findbugs")
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "org.json")
        exclude(module = "support-annotations")
        exclude(group = "com.google.guava")
    }
    
    // Miscellaneous
    implementation(libs.butterknife)
    implementation(libs.shimmer)
    implementation(libs.guava)
    implementation(libs.medallia)
    
    
    /////// Testing
    
    testImplementation(libs.junit.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.okhttp.mock.web.server)
    
    kaptAndroidTest(libs.hilt.kapt.test)
    
    androidTestImplementation(libs.junit.ktx)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.truth)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.bundles.espresso)
    androidTestImplementation(libs.hilt.android.test)
    
    debugImplementation(libs.fragment.test)
//    debugImplementation (libs.leakcanary)

    implementation ("com.github.douglasjunior:android-simple-tooltip:1.1.0")
//    implementation ("com.github.skydoves:balloon:1.5.4")
}