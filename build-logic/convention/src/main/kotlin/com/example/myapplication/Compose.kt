package com.example.myapplication

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

internal fun Project.configureAndroidCompose(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.apply {
        buildFeatures {
            compose = true
        }
    }
}

internal fun Project.configureAndroidCompose(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {
        buildFeatures {
            compose = true
        }
    }
}
