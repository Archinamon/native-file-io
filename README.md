[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.10-blue.svg)](http://kotlinlang.org) [ ![Download](https://api.bintray.com/packages/archinamon/maven/native-file-io/images/download.svg) ](https://bintray.com/archinamon/maven/native-file-io/_latestVersion)<br />

# native-file-io
File IO library based on Posix API for Kotlin/Native

Currently, contains only JVM and Posix (Linux X64 / MacOS X64) actual realisation.
In plans support mingw (Windows) archetype.

This library shares standard java file API to native environment, implementing Posix API.

# how to use it

```kotlin
// put this block somewhere in root build.gradle.kts file

allprojects {
  repositories {
      mavenCentral()
      maven("https://dl.bintray.com/archinamon/maven")
  }
}

// then in module's build.gradle.kts in target's dependencies section:

// expect; for kotlin common modules
implementation("me.archinamon:file-io:1.0")

// actual; demands on target type
implementation("me.archinamon:file-io-jvm:1.0") // for jvm module
implementation("me.archinamon:file-io-posix:1.0") // for linux/macos x64 posix module
```
