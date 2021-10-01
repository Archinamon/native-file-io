package me.archinamon.fileio

actual fun File.writeBinaryData(binary: ByteArray) {
    writeBytes(binary)
}