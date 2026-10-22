import com.android.build.api.dsl.ApplicationExtension
import com.example.myapplication.configureFlavors
import com.example.myapplication.configureKotlinAndroid
import com.example.myapplication.configureSpotless
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureFlavors(this)
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        val proguardFile = file("proguard-rules.pro")
                        if (proguardFile.exists()) {
                            proguardFiles(
                                getDefaultProguardFile("proguard-android-optimize.txt"),
                                proguardFile
                            )
                        } else {
                            proguardFiles(
                                getDefaultProguardFile("proguard-android-optimize.txt")
                            )
                        }
                    }
                }
            }
            configureSpotless()
        }
    }
}
