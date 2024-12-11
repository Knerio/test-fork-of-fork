import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-SNAPSHOT"
}

paperweight {
    upstreams.register("fork") {
        repo.set("/Users/jason/IdeaProjects/PaperMC/Softspoon/PatcherTest")
        ref.set("053b9646357235734b9eba6c01da7177c48848af")
        paper = false

        patchFile {
            path = "fork-server/build.gradle.kts"
            outputFile = file("forky-server/build.gradle.kts")
            patchFile = file("forky-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "fork-api/build.gradle.kts"
            outputFile = file("forky-api/build.gradle.kts")
            patchFile = file("forky-api/build.gradle.kts.patch")
        }
        patchDir {
            name = "paperApi"
            upstreamPath = "paper-api"
            patchesDir = file("forky-api/paper-patches")
            outputDir = file("paper-api")
            repo = true
        }
        patchDir {
            name = "paperApiGenerator"
            upstreamPath = "paper-api-generator"
            patchesDir = file("forky-api-generator/paper-patches")
            outputDir = file("paper-api-generator")
            repo = true
        }
        patchDir {
            name = "forkApi"
            upstreamPath = "fork-api"
            excludes = listOf("build.gradle.kts", "build.gradle.kts.patch")
            patchesDir = file("forky-api/fork-patches")
            outputDir = file("fork-api")
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
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
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
            /*
            maven("https://repo.papermc.io/repository/maven-snapshots/") {
                name = "paperSnapshots"
                credentials(PasswordCredentials::class)
            }
             */
        }
    }
}
