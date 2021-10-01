package me.archinamon.fileio

actual fun File.writeBinaryData(binary: ByteArray) {
    this.writeBytes(binary)
}