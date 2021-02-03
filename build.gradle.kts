plugins {
    kotlin("multiplatform") version "1.4.10"
    id("maven-publish")
}

group = "me.archinamon"
version = "1.0"

val isRunningInIde: Boolean = System.getProperty("idea.active")
    ?.toBoolean() == true

val isPublishingMode: String? by extra

repositories {
    mavenCentral()
}

kotlin {
    js { nodejs() }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    if (isPublishingMode?.toBoolean() != true) {
        // common code for both *nix platforms
        linuxX64("posix")
    }

    // generic linux code
    linuxX64()

    // darwin macos code
    macosX64 {
        val testApp: String? by extra

        if (testApp?.toBoolean() == true) {
            binaries {
                executable()
            }
        }
    }

//    mingwX64("windows") // not supported yet

    sourceSets {
        val macosX64Main by getting
        val linuxX64Main by getting

        if (isPublishingMode?.toBoolean() != true) {
            val posixMain by getting
            macosX64Main.dependsOn(posixMain)
            linuxX64Main.dependsOn(posixMain)
        } else {
            macosX64Main.kotlin.srcDir("src/posixMain")
            linuxX64Main.kotlin.srcDir("src/posixMain")
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        if (name == "posix")
            return@withType

        pom {
            name.set("file-io")
            description.set("Kotlin/Native file IO library with standard java-io interface")
            url.set("https://github.com/Archinamon/native-file-io")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/Archinamon/native-file-io")
                    distribution.set("repo")
                }
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("Archinamon")
                    name.set("Eduard Obolenskiy")
                    email.set("archinamon@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/Archinamon/native-file-io")
                connection.set("scm:git:https://github.com/Archinamon/native-file-io.git")
                developerConnection.set("scm:git:git@github.com:Archinamon/native-file-io.git")
            }
        }
    }

    repositories {
        if (isRunningInIde)
            return@repositories

        maven("https://api.bintray.com/maven/archinamon/maven/native-file-io/;publish=1;override=1") {
            credentials {
                val apiKey: String? by extra

                username = "Archinamon"
                password = apiKey ?: ""
            }
        }
    }
}
