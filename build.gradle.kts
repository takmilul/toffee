plugins {
    with(libs.plugins) {
        alias(ksp) apply false
        alias(kotlin.kapt) apply false
        alias(kotlin.gradle) apply false
        alias(android.library) apply false
        alias(android.application) apply false
        alias(google.services) apply false
        alias(kotlin.android) apply false
        alias(kotlin.parcelize) apply false
        alias(kotlin.serialize) apply false
        alias(navigation.safeargs) apply false
        alias(hilt.android) apply false
        alias(firebase.crashlytics) apply false
        alias(firebase.appdistribution) apply false
        alias(play.publisher) apply false
    }
}

buildscript {
    dependencies {
        classpath(libs.conviva.tracker.plugin)
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.version.get()))
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.isIncremental = true
}