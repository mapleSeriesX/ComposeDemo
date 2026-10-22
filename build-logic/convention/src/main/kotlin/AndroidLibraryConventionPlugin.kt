import com.android.build.api.dsl.LibraryExtension
import com.example.myapplication.configureDetekt
import com.example.myapplication.configureFlavors
import com.example.myapplication.configureKotlinAndroid
import com.example.myapplication.configureKtlint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureFlavors(this)
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                        val proguardFile = file("consumer-rules.pro")
                        if (proguardFile.exists()) {
                            consumerProguardFiles(proguardFile)
                        }
                    }
                }
            }
            configureDetekt()
            configureKtlint()
        }
    }
}
