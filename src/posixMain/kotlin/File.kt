package me.archinamon.fileio

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.FILENAME_MAX
import platform.posix.F_OK
import platform.posix.NULL
import platform.posix.O_APPEND
import platform.posix.O_CREAT
import platform.posix.O_RDWR
import platform.posix.R_OK
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.S_IFDIR
import platform.posix.S_IFREG
import platform.posix.S_IRWXG
import platform.posix.S_IRWXO
import platform.posix.S_IRWXU
import platform.posix.W_OK
import platform.posix.access
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.getcwd
import platform.posix.mkdir
import platform.posix.opendir
import platform.posix.readdir
import platform.posix.rmdir
import platform.posix.stat
import platform.posix.strlen
import platform.posix.unlink

actual class File actual constructor(
    private val pathname: String
) {

    private val fileSaperator
        get() = if (Platform.osFamily == OsFamily.WINDOWS) "\\" else "/"

    internal val modeRead = "r"
    private val modeAppend = "a"
    private val modeRewrite = "w"

    actual fun getParent(): String {
        return getAbsolutePath().substringBeforeLast(fileSaperator)
    }

    actual fun getParentFile(): File {
        return File(getParent())
    }

    actual fun getName(): String {
        return if (fileSaperator in pathname) {
            pathname.split(fileSaperator).last(String::isNotBlank)
        } else {
            pathname
        }
    }

    actual fun getAbsolutePath(): String {
        return if (!pathname.startsWith(fileSaperator)) {
            memScoped {
                getcwd(allocArray(FILENAME_MAX), FILENAME_MAX)
                    ?.toKString() + fileSaperator + pathname
            }
        } else pathname
    }

    actual fun lastModified(): Long = modified(this)

    actual fun mkdirs(): Boolean {
        if (!getParentFile().exists()) {
            getParentFile().mkdirs()
        }

        if (exists()) {
            return true
        }

        mkdir(pathname, (S_IRWXU or S_IRWXG or S_IRWXO).convert())
            .ensureUnixCallResult("mkdir") { ret -> ret == 0 }

        return true
    }

    actual fun createNewFile(): Boolean {
        if (exists()) {
            return true
        }

        fopen(pathname, modeRewrite).let { fd ->
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }

        return exists()
    }

    actual fun isFile(): Boolean {
        if (!exists()) {
            return false
        }

        return memScoped {
            val result = alloc<stat>()

            stat(pathname, result.ptr)
                .ensureUnixCallResult("stat") { ret -> ret == 0 }

            return@memScoped (result.st_mode.convert<Int>() and S_IFREG) == S_IFREG
        }
    }

    actual fun isDirectory(): Boolean {
        if (!exists()) {
            return false
        }

        return memScoped {
            val result = alloc<stat>()

            stat(pathname, result.ptr)
                .ensureUnixCallResult("stat") { ret -> ret == 0 }

            return@memScoped (result.st_mode.convert<Int>() and S_IFDIR) == S_IFDIR
        }
    }

    actual fun list(): Array<String> = memScoped {
        val dir = opendir(pathname)
            ?: return emptyArray()

        val result = ArrayList<String>()

        do {
            val record = readdir(dir)

            record?.pointed?.let { entity: dirent ->
                result.add(entity.d_name.toKString())
            }
        } while (record != NULL)

        closedir(dir).ensureUnixCallResult("closedir") { ret -> ret == 0 }

        return result.filter { name -> name !in arrayOf(".", "..") }
            .toTypedArray()
    }

    actual fun listFiles(): Array<File> = list().map { name ->
        val thisPath = getAbsolutePath().let { path ->
            if (!path.endsWith(fileSaperator)) {
                path + fileSaperator
            } else path
        }

        File(thisPath + name)
    }.toTypedArray()

    actual fun delete(): Boolean {
        if (isDirectory()) {
            return rmdir(pathname) == 0 // do not throw errors here
        }

        return unlink(pathname) == 0
    }

    actual fun exists(): Boolean {
        return access(pathname, F_OK) != -1
    }

    actual fun canRead(): Boolean {
        return access(getAbsolutePath(), R_OK) != -1
    }

    actual fun canWrite(): Boolean {
        return access(getAbsolutePath(), W_OK) != -1
    }

    internal fun writeBytes(bytes: ByteArray, mode: Int, size: ULong = ULong.MAX_VALUE, elemSize: ULong = 1U) {
        val fd = fopen(getAbsolutePath(), if (mode and O_APPEND == O_APPEND) modeAppend else modeRewrite)
        try {
            memScoped {
                bytes.usePinned { pinnedBytes ->
                    val bytesSize: ULong = if (size != ULong.MAX_VALUE) size else pinnedBytes.get().size.convert()
                    fwrite(pinnedBytes.addressOf(0), elemSize, bytesSize, fd)
                        .ensureUnixCallResult("fwrite") { ret -> ret == bytesSize }
                }
            }
        } finally {
            fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
        }
    }

    override fun toString(): String {
        return "File {\n" +
            "path=${getAbsolutePath()}\n" +
            "name=${getName()}\n" +
            "exists=${exists()}\n" +
            "canRead=${canRead()}\n" +
            "canWrite=${canWrite()}\n" +
            "isFile=${isFile()}\n" +
            "isDirectory=${isDirectory()}\n" +
            "lastModified=${lastModified()}\n" +
            (if (isDirectory()) "files=[${listFiles().joinToString()}]" else "") +
        "}"
    }
}

internal expect fun modified(file: File): Long

actual val File.mimeType: String
    get() = ""

actual fun File.readBytes(): ByteArray {
    val fd = fopen(getAbsolutePath(), modeRead)
    try {
        memScoped {
            fseek(fd, 0, SEEK_END)
            val size = ftell(fd).convert<Int>()
            fseek(fd, 0, SEEK_SET)

            return ByteArray(size + 1).also { buffer ->
                fread(buffer.refTo(0), 1UL, size.convert(), fd)
                    .ensureUnixCallResult("fread") { ret -> ret > 0U }
            }
        }
    } finally {
        fclose(fd).ensureUnixCallResult("fclose") { ret -> ret == 0 }
    }
}

actual fun File.readText(): String {
    return readBytes().toKString()
}

actual fun File.appendText(text: String) {
    writeBytes(text.encodeToByteArray(), O_RDWR or O_APPEND, strlen(text))
}

actual fun File.writeText(text: String) {
    writeBytes(text.encodeToByteArray(), O_RDWR or O_CREAT, strlen(text))
}
