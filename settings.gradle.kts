pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.google.com")
//        maven("https://repository.medallia.com/digital-maven/")
        maven("https://repo.eclipse.org/content/repositories/paho-releases/")
    }
}

include(":app", ":data", ":balloon")

rootProject.name = "ToffeeAndroidMobile"