plugins {
    alias(libs.plugins.wang.android.application)
}

android {
    namespace = "me.wangqiang.gamebox.app"


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.arouter.api)
    implementation(libs.arouter.compiler)
    //arouter需要这两个包
    implementation(libs.commons.collections4)
    implementation(libs.commons.lang3)

    implementation(project(":c_common"))
    implementation(project(":c_interfaces"))
    implementation(project(":m_home"))
    implementation(project(":m_tictactoe"))
}