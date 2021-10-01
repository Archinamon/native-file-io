package me.archinamon.fileio

import org.w3c.files.File as JsFile
import org.w3c.files.FileReaderSync

actual class File constructor(jsfile: JsFile) {

    internal var innerFile: JsFile = jsfile
    private var virtual: Boolean = false

    actual constructor(pathname: String) : this(JsFile(emptyArray(), pathname)) {
        virtual = true
    }

    actual fun getParent(): String? {
        return when {
            !exists() -> null
            "/" in innerFile.name -> innerFile.name
                .split("/")
                .let { if (it.size > 1) it[it.lastIndex - 1] else it.last() }
            else -> innerFile.name
        }
    }

    actual fun getParentFile(): File? {
        return getParent()?.run(::File)
    }

    actual fun getName(): String {
        return innerFile.name
    }

    actual fun lastModified(): Long {
        return innerFile.lastModified.toLong()
    }

    actual fun mkdirs(): Boolean {
        throw UnsupportedOperationException("Not available in JS!")
    }

    actual fun createNewFile(): Boolean {
        throw UnsupportedOperationException("Not available in JS!")
    }

    actual fun isFile(): Boolean {
        return true // always a file in js
    }

    actual fun isDirectory(): Boolean {
        return false
    }

    actual fun getAbsolutePath(): String {
        return innerFile.name
    }

    actual fun exists(): Boolean {
        return !virtual
    }

    actual fun canRead(): Boolean {
        return true
    }

    actual fun canWrite(): Boolean {
        return virtual && !innerFile.isClosed
    }

    actual fun list(): Array<String> {
        throw UnsupportedOperationException("Not available in JS!")
    }

    actual fun listFiles(): Array<File> {
        throw UnsupportedOperationException("Not available in JS!")
    }

    actual fun delete(): Boolean {
        throw UnsupportedOperationException("Not available in JS!")
    }
}

actual val File.mimeType: String
    get() = innerFile.type

actual fun File.readBytes(): ByteArray {
    return readText().encodeToByteArray()
}

actual fun File.readText(): String {
    return FileReaderSync().readAsText(innerFile)
}

actual fun File.appendText(text: String) {
    val newData = readText() + text
    writeText(newData)
}

actual fun File.writeText(text: String) {
    innerFile = JsFile(text.encodeToByteArray().toTypedArray(), innerFile.name)
}

actual fun File.writeBytes(bytes: ByteArray) {
    innerFile = JsFile(bytes.toTypedArray(), innerFile.name)
}
