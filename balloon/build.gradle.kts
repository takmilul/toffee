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
    
    buildTypes {
        getByName("debug") {
            isJniDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = false
            isJniDebuggable = false
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
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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