plugins {
    `kotlin-dsl`
}

group = "me.wangqiang.buildplugin"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(libs.android.tools.build.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        register("wangAndroidApplication") {
            id = "wang.android.application"
            implementationClass = "ApplicationConventionPlugin"
        }
        register("wangAndroidLibraryModule") {
            id = "wang.android.library.module"
            implementationClass = "ModuleConventionPlugin"
        }
        register("wangAndroidLibraryCLib") {
            id = "wang.android.library.c.lib"
            implementationClass = "CLibConventionPlugin"
        }
    }
}