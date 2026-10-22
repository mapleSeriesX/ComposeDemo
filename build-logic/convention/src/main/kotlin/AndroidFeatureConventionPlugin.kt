import com.android.build.api.dsl.LibraryExtension
import com.example.myapplication.configureAndroidCompose
import com.example.myapplication.configureHilt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.example.myapplication.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            configureHilt()

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
            }
        }
    }
}
