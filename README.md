# AppRemoteConfig for Android

Configure apps remotely: A simple but effective way to manage apps remotely.

Create a simple configuration file that is easy to maintain and host, yet provides important flexibility to specify settings based on your needs.

General info about AppRemoteConfig can be found [here](https://github.com/egeniq/app-remote-config).

### Build & Publishing Instructions

#### Local Development
To build the library and install it to your local Maven repository for testing:

```bash
./gradlew publishToMavenLocal
```

To build all platform artifacts (JVM and iOS):

```bash
./gradlew assemble
```

#### Publishing to GitHub Packages
To publish the library to GitHub Packages, you need to provide your GitHub credentials.

1. Create a file named `publish.properties` in the root directory of the project.
2. Add your GitHub username and a Personal Access Token (PAT) with `write:packages` scope:

```properties
token.name=YOUR_GITHUB_USERNAME
token.value=YOUR_GITHUB_TOKEN
```

3. Run the publishing task:

```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

> **Note:** The `publish.properties` file is excluded from Git by `.gitignore` to keep your credentials safe.

### Importing the Dependency

#### 1. Add the Repository

In your project's `settings.gradle.kts` (or `build.gradle.kts`):

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/egeniq/app-remote-config-android")
        credentials {
            username = "YOUR_GITHUB_USERNAME"
            password = "YOUR_GITHUB_TOKEN" // A GitHub Personal Access Token with 'read:packages' scope
        }
    }
}
```

#### 2. Add the Dependency

In your Android app's `build.gradle.kts` (or in the `commonMain` source set of a Kotlin Multiplatform project):

```kotlin
dependencies {
    implementation("com.egeniq:app-remote-config:0.4.2")
}
```
