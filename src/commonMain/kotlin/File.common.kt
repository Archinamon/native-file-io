package me.archinamon.fileio

expect class File(pathname: String) {
    fun getParent(): String?
    fun getParentFile(): File?

    fun getName(): String

    fun lastModified(): Long
    fun mkdirs(): Boolean
    fun createNewFile(): Boolean

    fun isFile(): Boolean
    fun isDirectory(): Boolean

    fun getAbsolutePath(): String
    fun length(): Long

    fun exists(): Boolean
    fun canRead(): Boolean
    fun canWrite(): Boolean

    fun list(): Array<String>
    fun listFiles(): Array<File>

    fun delete(): Boolean
}

expect val filePathSeparator: Char

val File.tempDirectory: String
    get() = "/tmp"

val File.tempFileType: String
    get() = ".tmp"

fun File.asParent(child: String): File {
    val path = getAbsolutePath() + filePathSeparator + child
    return File(path)
}

val File.nameWithoutExtension: String
    get() = getName().substringBeforeLast(".")

expect val File.mimeType: String

expect fun File.readBytes(): ByteArray

expect fun File.readText(): String

expect fun File.appendText(text: String)

expect fun File.appendBytes(bytes: ByteArray)

expect fun File.writeText(text: String)

expect fun File.createTempFile(prefix: String, suffix: String? = null): File

expect fun File.createTempFile(prefix: String, suffix: String? = null, dir: File): File

fun File.deleteRecursively(): Boolean = walkBottomUp()
    .fold(initial = true) { res, it ->
        (it.delete() || !it.exists()) && res
    }

fun File.getParentFileUnsafe(): File {
    return getParentFile()
        ?: getAbsolutePath()
            .substringBeforeLast(filePathSeparator)
            .run(::File)
}

fun File.validate() = run {
    print("Validating $nameWithoutExtension file...")

    if (!exists()) {
        println(); throw FileNotFoundException(getAbsolutePath(), "No such file or directory!")
    } else if (!canRead()) {
        println(); throw IllegalFileAccess(getAbsolutePath(), "Read access not granted!")
    } else if (!canWrite()) {
        println(); throw IllegalFileAccess(getAbsolutePath(), "Write access not granted!")
    }

    println(" OK!")
}
