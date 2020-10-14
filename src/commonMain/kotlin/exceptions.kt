package me.archinamon.fileio

open class FileIOException(fileName: String, reason: String) : Exception("FileIOException: $fileName ($reason)")

class FileNotFoundException(fileName: String, reason: String) : FileIOException(fileName, "FileNotFoundException: ($reason)")

class IllegalFileAccess(fileName: String, reason: String) : FileIOException(fileName, "Access denied: ($reason)")