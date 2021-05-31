package me.archinamon.fileio

import kotlin.test.*

class FileTests {

    @Test
    fun testNonexistentRootFile() {
        val testFile = File("testNonexistentRootFile.txt")

        assertFalse(testFile.exists(), "file should not exist")
        assertFalse(testFile.isDirectory(), "file should not be directory")
        assertFalse(testFile.isFile(), "file should not be file")
        assertNull(testFile.getParent(), "file should not have parent")
        assertNull(testFile.getParentFile(), "file should not have parent file")

        assertEquals("testNonexistentRootFile", testFile.nameWithoutExtension)
    }

    @Test
    fun testFileCreateAndDelete() {
        val testFolder = File("build/testNewDirectoryCreation")

        assertTrue(testFolder.mkdirs(), "create directory failed")
        assertTrue(testFolder.isDirectory(), "isDirectory check failed")

        val testFile = File("build/testNewDirectoryCreation/test.txt")
        assertTrue(testFile.createNewFile(), "create file failed")
        assertTrue(testFile.exists(), "file should exist")
        assertTrue(testFile.canRead(), "file should be readable")
        assertTrue(testFile.canWrite(), "file should be writable")
        assertTrue(testFile.isFile(), "file should be considered a file")
        assertFalse(testFile.isDirectory(), "file should not be considered a directory")
        assertTrue(testFile.delete(), "delete file failed")

        assertTrue(testFolder.delete(), "delete directory failed")
    }

    @Test
    fun testFileWriteAndRead() {
        val testFolder = File("build/testFileWriteAndRead")
        val testFile = File("build/testFileWriteAndRead/test.txt")

        assertTrue(testFolder.mkdirs(), "failed to create directories")
        assertTrue(testFile.createNewFile(), "failed to create file")

        val message1 = "Hello,"
        val message2 = " World!"
        testFile.writeText(message1)

        assertEquals(message1, testFile.readText(), "written text should match")

        testFile.appendText(message2)

        assertEquals(message1 + message2, testFile.readText(), "appended text should match")

        assertTrue(testFile.delete(), "failed to cleanup test file")
        assertTrue(testFolder.delete(), "failed to cleanup directory")
    }

    @Test
    fun testFileLists() {
        val testFolder = File("gradle/wrapper")
        val listedFiles = testFolder.listFiles().sortedBy { it.getName().length }
        val listedFileNames = testFolder.list().sortedBy { it.length }
        assertEquals(2, listedFiles.size, "required two files")
        assertEquals(2, listedFileNames.size, "required two file names")
        assertEquals("gradle-wrapper.jar", listedFileNames.first())
        assertEquals("gradle-wrapper.jar", listedFiles.first().getName())
        assertEquals("gradle-wrapper.properties", listedFileNames[1])
        assertEquals("gradle-wrapper.properties", listedFiles[1].getName())
    }
}
