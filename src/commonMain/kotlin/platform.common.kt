package me.archinamon.fileio

enum class Platform {
    Linux, Macos, Windows, JVM, JS;

    fun isPosix(): Boolean = this == Macos || this == Linux
}

expect fun platform(): Platform