package com.example.myapplication

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureSpotless() {
    pluginManager.apply("com.diffplug.spotless")
    extensions.configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint("1.2.1")
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.2.1")
        }
    }
}
