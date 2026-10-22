plugins {
    alias(libs.plugins.myapplication.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.example.data.preferences"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

tasks.configureEach {
    if (name.startsWith("ksp") && name.endsWith("Kotlin") && !name.contains("Test")) {
        val variantName = name.removePrefix("ksp").removeSuffix("Kotlin")
        dependsOn("generate${variantName}Proto")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { option("lite") }
                create("kotlin") { option("lite") }
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.protobuf.javalite)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
