import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    java
    id("org.jetbrains.intellij.platform")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion"))
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    pluginVerification {
        ides {
            create(
                IntelliJPlatformType.IntellijIdeaCommunity,
                providers.gradleProperty("platformVersion"),
            )
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 17
    }
}
