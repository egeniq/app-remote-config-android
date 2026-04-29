import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serializer)
    `maven-publish`
}

val publishInfo = Properties()
val publishPropertiesFile = rootProject.file("publish.properties")
if (publishPropertiesFile.exists()) {
    publishInfo.load(FileInputStream(publishPropertiesFile))
}

group = "com.egeniq"
version = getVersionFromFile("version.txt", "0.2.1")

kotlin {
    withSourcesJar()
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(21)

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            api(libs.kotlin.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/egeniq/app-remote-config-android")
            credentials {
                username = publishInfo["token.name"]?.toString() ?: ""
                password = publishInfo["token.value"]?.toString() ?: ""
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        // artifactId is automatically set for each target (e.g. AppRemoteConfig-jvm)
        pom {
            name = "App Remote Config"
            description = "A library that parses remote config values and provides them as a Kotlin API."
            url = "https://github.com/egeniq/app-remote-config-android"
            licenses {
                license {
                    name = "MIT License"
                    url = "https://github.com/egeniq/app-remote-config-android/blob/main/LICENSE"
                }
            }
        }
    }
}

fun getVersionFromFile(
    path: String,
    defaultValue: String,
): String {
    val versionFile = file(path)
    return if (versionFile.exists()) {
        versionFile.readText().trim()
    } else {
        defaultValue
    }
}
