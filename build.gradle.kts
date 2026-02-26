import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.20-RC"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("io.github.revxrsal.zapper") version "1.0.3"
}

group = "me.hhitt"
version = "1.4.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")

    zap("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    zap("com.h2database:h2:2.2.220")
    zap(kotlin("stdlib"))
    zap("org.jetbrains.exposed:exposed-core:0.57.0")
    zap("org.jetbrains.exposed:exposed-dao:0.57.0")
    zap("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    zap("io.github.revxrsal:lamp.common:4.0.0-beta.25")
    zap("io.github.revxrsal:lamp.bukkit:4.0.0-beta.25")
    zap("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.21.0")
    zap("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.21.0")

    implementation("fr.mrmicky:fastboard:2.1.5")
    implementation("dev.dejvokep:boosted-yaml:1.3.6")
}

zapper {
    libsFolder = "libs"
    repositories { includeProjectRepositories() }
}

val targetJavaVersion = 21

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        javaParameters = true
    }
}

