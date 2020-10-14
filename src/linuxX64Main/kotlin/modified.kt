package me.archinamon.fileio

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.stat

internal actual fun modified(file: File): Long = memScoped {
    val result = alloc<stat>()
    if (stat(file.getAbsolutePath(), result.ptr) != 0) {
        return 0L
    }

    result.st_mtim.tv_sec
}