@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.com.android.library.get().pluginId)
    id(libs.plugins.org.jetbrains.kotlin.android.get().pluginId)
}

android {
    namespace = "com.banglalink.toffee.balloon"
    compileSdk = 33
    
    defaultConfig {
        minSdk = 21
    }
    
    buildFeatures {
        viewBinding = true
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
    
    lint {
        abortOnError = false
    }
}

dependencies {
    api(libs.annotation)
    implementation(libs.activity)
    implementation(libs.fragment.ktx)
    implementation(libs.bundles.lifecycle)
}
