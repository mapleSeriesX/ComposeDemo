package com.example.myapplication

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

enum class FlavorDimension {
    environment
}

enum class EnvironmentFlavor(
    val dimension: FlavorDimension,
    val baseUrl: String,
    val applicationIdSuffix: String? = null
) {
    dev(
        dimension = FlavorDimension.environment,
        baseUrl = "https://mock-bdf67a3d1b63424cb4135762b6799da5.mock.insomnia.run/api/",
        applicationIdSuffix = ".dev"
    ),
    staging(
        dimension = FlavorDimension.environment,
        baseUrl = "https://staging-api.example.com/api/",
        applicationIdSuffix = ".staging"
    ),
    prod(
        dimension = FlavorDimension.environment,
        baseUrl = "https://api.example.com/api/"
    )
}

fun configureFlavors(
    applicationExtension: ApplicationExtension
) {
    applicationExtension.apply {
        buildFeatures.buildConfig = true
        flavorDimensions.add(FlavorDimension.environment.name)
        productFlavors {
            EnvironmentFlavor.entries.forEach { flavor ->
                create(flavor.name) {
                    dimension = flavor.dimension.name
                    buildConfigField("String", "BASE_URL", "\"${flavor.baseUrl}\"")
                    if (flavor.applicationIdSuffix != null) {
                        applicationIdSuffix = flavor.applicationIdSuffix
                    }
                }
            }
        }
    }
}

fun configureFlavors(
    libraryExtension: LibraryExtension
) {
    libraryExtension.apply {
        buildFeatures.buildConfig = true
        flavorDimensions.add(FlavorDimension.environment.name)
        productFlavors {
            EnvironmentFlavor.entries.forEach { flavor ->
                create(flavor.name) {
                    dimension = flavor.dimension.name
                    buildConfigField("String", "BASE_URL", "\"${flavor.baseUrl}\"")
                }
            }
        }
    }
}
