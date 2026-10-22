package com.example.myapplication

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import java.io.File

fun Project.configureDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        parallel = true
        buildUponDefaultConfig = true
        allRules = false
        val detektConfig = file("${rootProject.projectDir}/config/detekt/detekt.yml")
        if (detektConfig.exists()) {
            config.setFrom(detektConfig)
        }
    }

    val androidStudioJbr = File("J:/Program Files/Android/Android Studio/jbr")
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        if (androidStudioJbr.exists()) {
            jdkHome.set(androidStudioJbr)
        }
        reports {
            txt.required.set(true)
            txt.outputLocation.set(file("${rootProject.projectDir}/reports/${project.name}-detekt.txt"))
        }
        doFirst {
            System.setProperty("java.version", "21.0.0")
        }
    }
}
