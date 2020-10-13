package me.archinamon.fileio

expect class File(pathname: String) {
    fun getParentFile(): File

    fun getName(): String

    fun lastModified(): Long
    fun mkdirs(): Boolean
    fun isDirectory(): Boolean
    fun createNewFile(): Boolean

    fun getAbsolutePath(): String

    fun exists(): Boolean
    fun canRead(): Boolean
    fun canWrite(): Boolean
}

val File.nameWithoutExtension: String
    get() = getName().substringBeforeLast(".")

expect fun File.readText(): String

expect fun File.appendText(text: String)

expect fun File.writeText(text: String)

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
