package me.archinamon.fileio

fun File.copyTo(dest: String, overwrite: Boolean) {
    this.copyTo(File(dest), overwrite)
}

fun File.copyTo(dest: File, overwrite: Boolean) {
    if (this.isDirectory()) {
        throw UnsupportedOperationException("Moving/copying directories directly not allowed!")
    }

    this.readBytes().let { bytes ->
        dest.apply {
            if (exists() && !overwrite) {
                throw IllegalFileAccess(dest.getAbsolutePath(), "Already exists and not allowed to be overwritten!")
            }

            if (!exists()) {
                createNewFile()
            }

            writeBytes(bytes)
        }
    }
}

fun File.moveTo(dest: String, overwrite: Boolean) {
    this.copyTo(dest, overwrite)
    this.delete()
}

fun File.moveTo(dest: File, overwrite: Boolean) {
    this.copyTo(dest, overwrite)
    this.delete()
}

expect fun File.writeBytes(bytes: ByteArray)