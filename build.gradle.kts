@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.com.gms.google.services) apply false
    alias(libs.plugins.com.google.firebase.crashlytics) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.com.google.dagger.hilt.android) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}