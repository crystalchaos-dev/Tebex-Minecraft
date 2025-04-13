import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

fun gitCommitHash(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .redirectErrorStream(true)
            .start()
        val result = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0) result else "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

plugins {
    java
    id("com.gradleup.shadow") version "8.3.1"
}

defaultTasks("shadowJar")

group = "io.tebex"
version = "2.2.1"

val processedSourcesDir = layout.buildDirectory.dir("processedSources")

val processSources by tasks.registering(Copy::class) {
    val props = mapOf("@VERSION@" to rootProject.version)
    from("src/main/java")
    into(processedSourcesDir)
    filteringCharset = "UTF-8"
    expand(props)
    outputs.dir(processedSourcesDir)
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(processSources)
    source(processedSourcesDir)
}

subprojects {
    plugins.apply("java")
    plugins.apply("com.gradleup.shadow")
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.named<ShadowJar>("shadowJar") {
        val suffix = if (project.name == "bukkit") "-Folia" else ""
        archiveFileName.set("tebex-${project.name}-${rootProject.version}-${gitCommitHash()}$suffix.jar")
    }

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            name = "spigotmc-repo"
        }
        maven("https://oss.sonatype.org/content/groups/public/") {
            name = "sonatype"
        }
        maven("https://repo.opencollab.dev/main/") {
            name = "opencollab-snapshot-repo"
        }
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "paper-repo"
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            name = "extendedclip-repo"
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-snapshots"
        }
        maven("https://maven.nucleoid.xyz/") {
            name = "nucleoid"
        }
        maven("https://repo.xyrisdev.com/repository/maven-public") {
            name = "xyris-repo"
        }
    }

    tasks.named("processResources", Copy::class.java) {
        val props = mapOf("version" to rootProject.version, "@VERSION@" to rootProject.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        filesNotMatching("**/*.zip") {
            expand(props)
        }
    }
}