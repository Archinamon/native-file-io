package me.archinamon.fileio

object Files {
    private const val tempDirectory = "/tmp"
    private const val tempFileType = ".tmp"

    fun createTempFile(prefix: String, suffix: String? = null): File {
        return createTempFile(prefix, suffix, File(tempDirectory))
    }

    fun createTempFile(prefix: String, suffix: String? = null, dir: File): File {
        if (platform() == Platform.Windows) {
            throw UnsupportedOperationException("Do not supported on mingw yet, create tmp files/dirs manually!")
        }

        val parent = dir.getPath()
        dir.mkdirs()

        if (!dir.canWrite()) {
            throw IllegalFileAccess(parent, "Can't create file in the directory")
        }

        if (prefix.length < 3) {
            throw IllegalArgumentException("prefix should be at least 3 chars long, now — ${prefix.length}")
        }

        val end = suffix ?: tempFileType
        return File("$parent/$prefix$end").apply {
            createNewFile()
        }
    }
}