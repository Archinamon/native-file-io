package me.archinamon.fileio

import kotlinx.cinterop.*
import platform.windows.*

// Difference between January 1, 1601 and January 1, 1970 in millis
private const val EPOCH_DIFF = 11644473600000
private const val ERR_SIZE = -1L

actual class File actual constructor(pathname: String) {

    private val pathname: String = pathname.replace('/', filePathSeparator)

    actual fun getParent(): String? {
        return if (exists()) getAbsolutePath().substringBeforeLast(filePathSeparator) else null
    }

    actual fun getParentFile(): File? {
        return getParent()?.run(::File)
    }

    actual fun getName(): String {
        return if (filePathSeparator in pathname) {
            pathname.split(filePathSeparator).last(String::isNotBlank)
        } else {
            pathname
        }
    }

    actual fun lastModified(): Long {
        val handle = CreateFileA(
            pathname,
            GENERIC_READ,
            0,
            null,
            OPEN_EXISTING,
            FILE_ATTRIBUTE_NORMAL,
            null
        )
        if (handle == INVALID_HANDLE_VALUE) {
            return 0L
        }

        return try {
            memScoped {
                val ft = alloc<_FILETIME>()
                val result = GetFileTime(handle, null, null, ft.ptr)
                if (result == TRUE) {
                    val st = alloc<_SYSTEMTIME>()
                    val convertResult = FileTimeToSystemTime(ft.ptr, st.ptr)
                    if (convertResult == TRUE) {
                        val time = (ft.dwHighDateTime.toLong() shl 32) or ft.dwLowDateTime.toLong()
                        (time / 10000) - EPOCH_DIFF
                    } else 0L
                } else 0L
            }
        } finally {
            CloseHandle(handle)
        }
    }

    actual fun mkdir(): Boolean {
        if (getParentFile()?.exists() != true) {
            return false
        }

        if (getParentFile()?.canWrite() != true) {
            throw IllegalFileAccess(pathname, "Directory not accessible for write operations")
        }

        return SHCreateDirectoryExA(null, getAbsolutePath(), null) == ERROR_SUCCESS
    }

    actual fun mkdirs(): Boolean {
        if (exists()) {
            return false
        }

        if (mkdir()) {
            return true
        }

        return (getParentFile()?.mkdirs() == true || getParentFile()?.exists() == true) && mkdir()
    }

    actual fun createNewFile(): Boolean {
        val handle = CreateFileA(
            pathname,
            GENERIC_WRITE,
            FILE_SHARE_READ,
            null,
            CREATE_NEW,
            FILE_ATTRIBUTE_NORMAL,
            null
        )

        return try {
            handle != INVALID_HANDLE_VALUE
        } finally {
            CloseHandle(handle)
        }
    }

    actual fun isFile(): Boolean {
        return GetFileAttributesA(pathname).let { attrs ->
            attrs != INVALID_FILE_ATTRIBUTES &&
                attrs and FILE_ATTRIBUTE_DIRECTORY.toUInt() == 0u
        }
    }

    actual fun isDirectory(): Boolean {
        return GetFileAttributesA(pathname).let { attrs ->
            attrs != INVALID_FILE_ATTRIBUTES &&
                (attrs and FILE_ATTRIBUTE_DIRECTORY.toUInt() != 0u)
        }
    }

    actual fun getPath(): String {
        return pathname
    }

    actual fun getAbsolutePath(): String {
        return if (pathname.startsWith(filePathSeparator) || pathname.getOrNull(1) == ':') {
            pathname
        } else {
            memScoped {
                val bufLength = 200
                val buf = allocArray<ByteVar>(bufLength)
                val result = GetCurrentDirectoryA(bufLength.toUInt(), buf).toInt()
                check(result != 0)
                if (result > bufLength) {
                    val retryBuf = allocArray<ByteVar>(result)
                    GetCurrentDirectoryA(result.toUInt(), buf)
                    check(result != 0)
                    retryBuf.toKString()
                } else {
                    buf.toKString()
                } + filePathSeparator + pathname
            }
        }
    }

    actual fun length(): Long = memScoped {
        val handle = CreateFileA(
            getAbsolutePath(),
            GENERIC_READ,
            FILE_SHARE_READ,
            null,
            OPEN_EXISTING,
            0,
            null
        )
        if (handle == INVALID_HANDLE_VALUE) return ERR_SIZE

        return try {
            val fs = alloc<_LARGE_INTEGER>()
            if (GetFileSizeEx(handle, fs.ptr) == TRUE) {
                val size = (fs.HighPart.toUInt() shl 32) or fs.LowPart

                size.toLong()
            } else {
                ERR_SIZE
            }
        } finally {
            CloseHandle(handle)
        }
    }

    actual fun exists(): Boolean {
        return GetFileAttributesA(pathname) != INVALID_FILE_ATTRIBUTES
    }

    actual fun canRead(): Boolean {
        val handle = CreateFileA(
            pathname,
            GENERIC_READ,
            FILE_SHARE_READ,
            null,
            OPEN_EXISTING,
            0,
            null
        )

        return try {
            handle != INVALID_HANDLE_VALUE
        } finally {
            CloseHandle(handle)
        }
    }

    actual fun canWrite(): Boolean {
        val handle = CreateFileA(
            pathname,
            GENERIC_WRITE,
            FILE_SHARE_WRITE,
            null,
            OPEN_EXISTING,
            0,
            null
        )

        return try {
            handle != INVALID_HANDLE_VALUE
        } finally {
            CloseHandle(handle)
        }
    }

    actual fun list(): Array<String> = memScoped {
        if (isFile()) return emptyArray()

        val findData = alloc<WIN32_FIND_DATAA>()
        val searchPath = if (pathname.endsWith(filePathSeparator)) {
            pathname
        } else {
            "$pathname${filePathSeparator}"
        } + "*"
        val find = FindFirstFileA(searchPath, findData.ptr)
        if (find == INVALID_HANDLE_VALUE) {
            return emptyArray()
        }

        val files = mutableListOf<String>()
        try {
            while (FindNextFileA(find, findData.ptr) != 0) {
                val fileName = findData.cFileName.toKString()
                if (fileName != "..") {
                    files.add(fileName)
                }
            }
        } finally {
            FindClose(find)
        }

        return files.toTypedArray()
    }

    actual fun listFiles(): Array<File> {
        if (isFile()) return emptyArray()
        val thisPath = getAbsolutePath().let { path ->
            if (!path.endsWith(filePathSeparator)) {
                path + filePathSeparator
            } else path
        }
        return list()
            .map { name -> File(thisPath + name) }
            .toTypedArray()
    }

    actual fun delete(): Boolean = memScoped {
        if (isFile()) return DeleteFileA(pathname) == TRUE

        val fileOp = alloc<SHFILEOPSTRUCTA> {
            hwnd = null
            wFunc = FO_DELETE.toUInt()
            pFrom = pathname.cstr.ptr
            pTo = null
            fFlags = (FOF_SILENT or FOF_NOCONFIRMATION or FOF_NOERRORUI).toUShort()
            fAnyOperationsAborted = FALSE
            hNameMappings = null
            lpszProgressTitle = null
        }

        return SHFileOperationA(fileOp.ptr) == 0
    }

    internal fun writeBytes(bytes: ByteArray, access: Int) {
        val handle = CreateFileA(
            getAbsolutePath(),
            access.toUInt(),
            FILE_SHARE_WRITE,
            null,
            OPEN_EXISTING,
            0,
            null
        )
        if (handle == INVALID_HANDLE_VALUE) return

        try {
            memScoped {
                val bytesWritten = alloc<UIntVar>()
                bytes.usePinned { b ->
                    WriteFile(
                        handle,
                        b.addressOf(0),
                        bytes.size.toUInt(),
                        bytesWritten.ptr,
                        null
                    )
                }
            }
        } finally {
            CloseHandle(handle)
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

actual val filePathSeparator by lazy { if (platform() == Platform.Windows) '\\' else '/' }

actual val File.mimeType: String
    get() = ""

actual fun File.readBytes(): ByteArray = memScoped {
    val handle = CreateFileA(
        getAbsolutePath(),
        GENERIC_READ,
        FILE_SHARE_READ,
        null,
        OPEN_EXISTING,
        0,
        null
    )
    if (handle == INVALID_HANDLE_VALUE) return byteArrayOf()

    try {
        val fs = alloc<_LARGE_INTEGER>()
        if (GetFileSizeEx(handle, fs.ptr) == TRUE) {
            val size = (fs.HighPart.toUInt() shl 32) or fs.LowPart
            val buf = allocArray<ByteVar>(size.toInt())
            val bytesRead = alloc<UIntVar>()
            if (ReadFile(handle, buf, size, bytesRead.ptr, null) == TRUE) {
                buf.readBytes(bytesRead.value.toInt())
            } else {
                byteArrayOf()
            }
        } else {
            byteArrayOf()
        }
    } finally {
        CloseHandle(handle)
    }
}

actual fun File.writeBytes(bytes: ByteArray) {
    // no need to use pinning or memscope, cause it's inside the method already does
    writeBytes(bytes, GENERIC_WRITE)
}

actual fun File.readText(): String {
    return readBytes().toKString()
}

actual fun File.appendText(text: String) {
    writeBytes(text.encodeToByteArray(), FILE_APPEND_DATA)
}

actual fun File.appendBytes(bytes: ByteArray) {
    writeBytes(bytes, FILE_APPEND_DATA)
}

actual fun File.writeText(text: String) {
    writeBytes(text.encodeToByteArray(), GENERIC_WRITE)
}
