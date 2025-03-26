package me.wangqiang.buildplugin.config

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

@Suppress("UnstableApiUsage")
internal fun configureAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = 35
        defaultConfig {
            minSdk = 23
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        buildFeatures {
            buildConfig = true
            dataBinding {
                enable = true
            }
        }
    }
}

@Suppress("UnstableApiUsage")
internal fun configureARouter(extensions: ExtensionContainer, target: Project) {
    extensions.apply {
        val kaptExtension = getByType<KaptExtension>()
        kaptExtension.apply {
            correctErrorTypes = true
            arguments {
                arg("AROUTER_MODULE_NAME", target.name)
            }
        }
    }
}