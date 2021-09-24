[![Kotlin](https://img.shields.io/badge/Kotlin-1.5.10-blue.svg)](http://kotlinlang.org) ![Maven Central](https://img.shields.io/maven-central/v/me.archinamon/file-io?style=flat-square)
<br />

# native-file-io
File IO library based on Posix API for Kotlin/Native

Currently, contains only JS, JVM and Posix (Linux X64 / MacOS X64) actual realisation.
In plans support mingw (Windows) archetype.

This library shares standard java file API to native environment, implementing Posix API.

# how to use it

```kotlin
// put this block somewhere in root build.gradle.kts file

allprojects {
  repositories {
      mavenCentral()
  }
}

// then in module's build.gradle.kts in target's dependencies section:
val fileIoVersion: String by extra // reads from gradle.properties

// expect; for kotlin common modules
implementation("me.archinamon:file-io:$fileIoVersion")

// actual; demands on target type
implementation("me.archinamon:file-io-jvm:$fileIoVersion") // for jvm module
implementation("me.archinamon:file-io-js:$fileIoVersion") // for kotlin-js module
implementation("me.archinamon:file-io-linuxx64:$fileIoVersion") // for linux x64 posix module
implementation("me.archinamon:file-io-macosx64:$fileIoVersion") // for macOS x64 posix module
implementation("me.archinamon:file-io-mingwx64:$fileIoVersion") // for windows x64 module
```
