plugins {
    with(libs.plugins) {
        alias(android.application) apply false
        alias(android.library) apply false
        alias(kotlin.android) apply false
        alias(kotlin.gradle) apply false
        alias(ksp) apply false
        alias(kotlin.kapt) apply false
        alias(google.services) apply false
        alias(navigation.safeargs) apply false
        alias(hilt.android) apply false
        alias(firebase.crashlytics) apply false
    }
}

buildscript {
    dependencies {
        classpath(libs.conviva.tracker.plugin)
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.version.get()))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}