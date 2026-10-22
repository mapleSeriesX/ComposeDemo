package com.example.myapplication

import org.gradle.api.Project

internal fun Project.configureHilt() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
        apply("com.google.dagger.hilt.android")
    }
}
