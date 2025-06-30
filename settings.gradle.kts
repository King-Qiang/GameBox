pluginManagement {
    includeBuild("build_plugin")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GameBox"
include(":app")
include(":c_common")
include(":m_home")
include(":m_tictactoe")
include(":c_interfaces")
include(":m_tzfe")
include(":m_gobang")
include(":m_flappybird")
