package me.archinamon.fileio

import kotlinx.cinterop.convert
import platform.posix.O_RDWR

actual fun File.writeBinaryData(binary: ByteArray) {
    // no need to use pinning or memscope, cause it's inside the method already does
    writeBytes(binary, O_RDWR, binary.size.convert(), Byte.SIZE_BYTES.convert())
}