import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "claude-standby-jetbrains"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.18.1"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}
