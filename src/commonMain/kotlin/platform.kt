package me.archinamon.fileio

enum class Platform {
    Linux, Macos, Windows, JVM, JS
}

expect fun platform(): Platform