plugins {
    kotlin("multiplatform") version "1.4.10"
}

group = "me.archinamon"
version = "1.0"

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
        "Mac OS X" -> macosX64("native")
        "Linux" -> linuxX64("native")
//        isMingwX64 -> mingwX64("native") // not supported yet
        else -> throw GradleException("Host OS is not supported in File-IO project.")
    }
}