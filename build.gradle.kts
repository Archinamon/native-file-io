plugins {
    kotlin("multiplatform") version "1.4.10"
    id("maven-publish")
}

group = "me.archinamon"
version = "1.0"

val os by lazy { org.gradle.internal.os.OperatingSystem.current()!! }
val isRunningInIde: Boolean = System.getProperty("idea.active") == "true"

repositories {
    mavenCentral()
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when (hostOs) {
        "Mac OS X" -> macosX64("posix")
        "Linux" -> linuxX64("posix")
//        isMingwX64 -> mingwX64("windows") // not supported yet
        else -> throw GradleException("Host OS is not supported in File-IO project.")
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
        maven("https://api.bintray.com/maven/archinamon/maven/native-file-io/;publish=0;override=1") {
            credentials {
                username = "Archinamon"
                password = extra["apiKey"] as? String ?: ""
            }
        }
    }
}
