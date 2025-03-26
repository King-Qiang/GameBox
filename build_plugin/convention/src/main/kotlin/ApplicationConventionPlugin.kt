import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import me.wangqiang.buildplugin.config.configureARouter
import me.wangqiang.buildplugin.config.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Properties

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.kapt")
            }
            extensions.configure<BaseAppModuleExtension> {
                configureAndroid(commonExtension = this)
                val propertiesFile = File(target.rootDir, "common.properties")
                val properties = Properties().apply {
                    load(propertiesFile.inputStream())
                }
                defaultConfig {
                    compileSdk = 35
                    targetSdk = 34
                    applicationId = properties.getProperty("APP_PACKAGE_NAME")
                    versionName = properties.getProperty("APP_VERSION_NAME")
                    versionCode = properties.getProperty("APP_VERSION_CODE").toIntOrNull()
                }
            }
            configureARouter(extensions, target)
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {

            }
        }
    }
}