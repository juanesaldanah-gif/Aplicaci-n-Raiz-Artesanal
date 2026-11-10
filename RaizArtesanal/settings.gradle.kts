pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "RaizArtesanal"
include(
    ":app",
    ":core:ui",
    ":core:data",
    ":core:network",
    ":core:security",
    ":core:device",
    ":feature:auth",
    ":feature:catalog",
    ":feature:cart",
    ":feature:payments",
    ":feature:profile",
    ":feature:admin"
)
