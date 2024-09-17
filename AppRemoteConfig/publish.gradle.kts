import java.io.FileInputStream
import java.util.Properties

apply(plugin = "maven-publish")

val publishInfo = Properties()
publishInfo.load(FileInputStream(File("publish.properties")))
val libVersion = getVersionFromFile("version.txt", "0.2.1")

configure<PublishingExtension> {
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/egeniq/app-remote-config-android")
            credentials {
                username = publishInfo["token.name"].toString()
                password = publishInfo["token.value"].toString()
            }
        }

        publications {
            register<MavenPublication>("gpr") {
                groupId = "com.egeniq"
                artifactId = "app-remote-config"
                version = libVersion

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}

fun getVersionFromFile(
    path: String,
    defaultValue: String,
): String {
    val versionFile = File(path)
    val value =
        if (versionFile.exists()) {
            versionFile.readText().trim()
        } else {
            defaultValue
        }
    return value
}
