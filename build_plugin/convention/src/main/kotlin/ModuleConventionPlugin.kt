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
                add("implementation", project(":c_interfaces"))
                add("implementation", project(":c_common"))
                add("implementation", libs.findLibrary("commons.collections4").get())
                add("implementation", libs.findLibrary("arouter.api").get())
                add("kapt", libs.findLibrary("arouter.compiler").get())
                add("implementation", platform(libs.findLibrary("compose.bom").get()))
                add("implementation", libs.findLibrary("compose.ui").get())
                add("implementation", libs.findLibrary("compose.ui.graphics").get())
                add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
                add("implementation", libs.findLibrary("compose.material3").get())
                add("implementation", libs.findLibrary("compose.activity").get())
                add("debugImplementation", libs.findLibrary("compose.ui.tooling").get())
                add("debugImplementation", libs.findLibrary("compose.ui.test.manifest").get())
            }
        }
    }
}