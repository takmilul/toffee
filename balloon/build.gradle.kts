plugins {
    with(libs.plugins) {
        alias(android.library)
        alias(kotlin.android)
    }
}

android {
    namespace = "com.banglalink.toffee.balloon"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isJniDebuggable = false
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
    with(libs) {
        api(annotation)
        implementation(activity)
        implementation(fragment.ktx)
        implementation(bundles.lifecycle)
    }
}