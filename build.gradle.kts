import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy.INCLUDE

plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.run.paper)
}

group = "de.ole101.marketplace"
version = "1.0.0-SNAPSHOT"
description = "An Item Marketplace Plugin."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.annotations)

    implementation(libs.gson)
    implementation(libs.guice)
    implementation(libs.mongodb)
    implementation(libs.discord.webhooks)

    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    named<ShadowJar>("shadowJar") {
        archiveFileName.set("${project.name}.jar")
    }

    build {
        dependsOn(named("shadowJar"))
    }

    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("paper-plugin.yml") {
                expand("version" to project.version)
            }
            duplicatesStrategy = INCLUDE
        }
    }

    runServer {
        minecraftVersion("1.21.4")
    }
}
