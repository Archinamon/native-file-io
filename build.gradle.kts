plugins {
    kotlin("multiplatform") version "1.5.10"
    id("maven-publish")
}

group = "me.archinamon"
version = "1.2"

val isRunningInIde: Boolean = System.getProperty("idea.active")
    ?.toBoolean() == true

repositories {
    mavenCentral()
}

kotlin {
    js { nodejs() }

    jvm()

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
        val commonMain by getting
        val posixMain by creating {
            dependsOn(commonMain)
        }
        val macosX64Main by getting {
            dependsOn(posixMain)
        }
        val linuxX64Main by getting {
            dependsOn(posixMain)
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
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
