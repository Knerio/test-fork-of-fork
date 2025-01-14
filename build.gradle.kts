import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.13"
}

paperweight {
    upstreams.register("purpur") {
        repo = github("PurpurMC", "Purpur")
        ref = providers.gradleProperty("purpurRef")

        patchFile {
            path = "purpur-server/build.gradle.kts"
            outputFile = file("forky-server/build.gradle.kts")
            patchFile = file("forky-server/build.gradle.kts.patch")
            rejectsFile = file("forky-server/rejects")
        }
        patchFile {
            path = "purpur-api/build.gradle.kts"
            outputFile = file("forky-api/build.gradle.kts")
            patchFile = file("forky-api/build.gradle.kts.patch")
            rejectsFile = file("forky-api/rejects")
        }
        patchRepo("paperApi") {
            upstreamPath = "paper-api"
            patchesDir = file("forky-api/paper-patches")
            outputDir = file("paper-api")
        }
        patchRepo("paperApiGenerator") {
            upstreamPath = "paper-api-generator"
            patchesDir = file("forky-api-generator/paper-patches")
            outputDir = file("paper-api-generator")
        }
        patchDir("purpurApi") {
            upstreamPath = "purpur-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch", "paper-patches")
            patchesDir = file("forky-api/purpur-patches")
            outputDir = file("purpur-api")
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven {
            url = uri("https://reposilite.varilx.de/releases")
            credentials {
                username = System.getenv("VARILX_USER")
                password = System.getenv("VARILX_TOKEN")
            }
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven("https://reposilite.varilx.de/releases") {
                name = "varilx-releases"
                credentials {
                    username = System.getenv("VARILX_USER")
                    password = System.getenv("VARILX_TOKEN")
                }
            }
        }
    }
}
