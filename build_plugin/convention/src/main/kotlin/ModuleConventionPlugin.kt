import com.android.build.gradle.LibraryExtension
import me.wangqiang.buildplugin.config.configureARouter
import me.wangqiang.buildplugin.config.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class ModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.kapt")
            }

            extensions.configure<LibraryExtension> {
                configureAndroid(this)
                defaultConfig.targetSdk = 34
                defaultConfig.vectorDrawables.useSupportLibrary = true
            }
            configureARouter(extensions, target)

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("implementation", project(":c_common"))
            }
        }
    }
}