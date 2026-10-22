package com.example.myapplication

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

fun Project.configureKtlint() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    extensions.configure<KtlintExtension> {
        version.set("1.2.1")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude { it.file.path.contains("build/") }
        }
    }
}
