package me.archinamon.fileio

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.DIR
import platform.posix.dirent
import platform.posix.stat

internal actual fun stat.checkFileIs(flag: Int): Boolean = (st_mode.convert<Int>() and flag) == flag

internal actual fun mkdir(path: String, mode: UInt): Int = platform.posix.mkdir(path, mode)

internal actual fun opendir(path: String): CPointer<out CPointed>? = platform.posix.opendir(path)

@Suppress("UNCHECKED_CAST")
internal actual fun readdir(dir: CPointer<out CPointed>): CPointer<dirent>? = platform.posix.readdir(dir as CPointer<DIR>)

@Suppress("UNCHECKED_CAST")
internal actual fun closedir(dir: CPointer<out CPointed>): Int = platform.posix.closedir(dir as CPointer<DIR>)

internal actual fun modified(file: File): Long = memScoped {
    val result = alloc<stat>()
    if (stat(file.getAbsolutePath(), result.ptr) != 0) {
        return 0L
    }

    result.st_mtim.tv_sec
}