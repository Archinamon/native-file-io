package me.archinamon.fileio

import platform.windows.GENERIC_WRITE

actual fun File.writeBinaryData(binary: ByteArray) {
    // no need to use pinning or memscope, cause it's inside the method already does
    writeBytes(binary, GENERIC_WRITE)
}