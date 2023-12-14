@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    with(libs.plugins) {
        alias(ksp) apply false
        alias(kotlin.gradle) apply false
        alias(com.android.library) apply false
        alias(com.android.application) apply false
        alias(com.gms.google.services) apply false
        alias(org.jetbrains.kotlin.android) apply false
        alias(androidx.navigation.safeargs) apply false
        alias(com.google.dagger.hilt.android) apply false
        alias(com.google.firebase.crashlytics) apply false
    }
}

buildscript {
    dependencies {
        classpath(libs.conviva.tracker.plugin)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}