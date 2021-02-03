@file:Suppress("PackageDirectoryMismatch")
package me.archinamon.fileio

import java.net.URLConnection
import java.nio.charset.Charset
import kotlin.io.readBytes as kReadBytes

actual typealias File = java.io.File

actual val File.mimeType: String
    get() = URLConnection.guessContentTypeFromName(name)

actual fun File.readBytes() = kReadBytes()

actual fun File.readText() = readText(Charset.defaultCharset())

actual fun File.appendText(text: String) = appendText(text, Charset.defaultCharset())

actual fun File.writeText(text: String) = writeText(text, Charset.defaultCharset())