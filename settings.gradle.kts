pluginManagement {
    includeBuild("build-logic")
    repositories {
                google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
               google()
        mavenCentral()
    }
}

rootProject.name = "ComposeDemo"
include(":app")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:common")
include(":data:user")
include(":feature:user")
include(":feature:userDetail")
include(":data:preferences")
include(":benchmark")
