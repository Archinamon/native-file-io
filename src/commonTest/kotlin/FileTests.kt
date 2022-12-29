package me.archinamon.fileio

import kotlin.test.*

class FileTests {

    private val localSeparator = '/'

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
    fun testExistentRootFile() {
        val testFile = File("testFileRoot/testExistentRootFile.txt")
        println(testFile.getAbsolutePath())

        assertFalse(testFile.exists(), "file should not exist")
        assertFalse(testFile.getParentFile()?.exists() == true, "file should not have parent file")
        assertNotNull(testFile.getParentFileUnsafe(), "file should not have parent file")

        assertEquals("testFileRoot", testFile.getParentFileUnsafe().getName(), "couldn't get parent file name")
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

    @Test
    fun testFileCopyMethod() {
        val testFile = File("gradle/wrapper/gradle-wrapper.properties")
        val testDestFolder = File("build/testCopyFolder")
        val testDestFile = File("build/testCopyFolder/gradle-wrapper.properties")

        assertTrue(testFile.exists(), "file have to exist")
        assertFalse(testDestFolder.exists(), "folder shouldn't be created yet")

        testDestFolder.mkdirs()
        assertTrue(testDestFolder.exists(), "now folder have to exists")

        assertFalse(testDestFile.exists(), "file should not exists yet")
        testFile.copyTo(testDestFile, overwrite = false)
        assertTrue(testDestFile.exists(), "file have to exist after coping")

        assertTrue(testDestFile.delete(), "failed to cleanup test file")
        assertTrue(testDestFolder.delete(), "failed to cleanup directory")
    }

    @Test
    fun testFileMoveMethod() {
        val testFolder = File("build/testMoveFolder")
        val testDestFolder = File("build/testMoveFolder2")
        val testFile = File("build/testMoveFolder/test_move_file.properties")
        val testDestFile = File("build/testMoveFolder2/test_move_file.properties")

        assertFalse(testFile.exists(), "file have not to exist")
        assertFalse(testFolder.exists(), "folder shouldn't be created yet")
        assertFalse(testDestFolder.exists(), "folder shouldn't be created yet")

        assertTrue(testFolder.mkdirs() && testFolder.exists(), "now folder1 have to exists")
        assertTrue(testDestFolder.mkdirs() && testDestFolder.exists(), "now folder2 have to exists")

        assertTrue(testFile.createNewFile(), "file should be created")
        testFile.writeText("moving=true")

        testFile.moveTo(testDestFile, overwrite = false)
        assertTrue(testDestFile.exists(), "file have to exist after coping")
        assertEquals("moving=true", testDestFile.readText(), "moved file should have the same content")
        assertFalse(testFile.exists(), "file should not exists on previous place after moving")

        assertTrue(testFolder.delete(), "failed to cleanup test file")
        assertTrue(testDestFile.delete(), "failed to cleanup test file")
        assertTrue(testDestFolder.delete(), "failed to cleanup directory")
    }

    @Test
    fun testCreateTempFileAndDelete() {
        val testFile = Files.createTempFile("test")

        assertEquals("/tmp/test.tmp", testFile.getAbsolutePath())
        assertTrue(testFile.exists(), "file should exist")
        assertTrue(testFile.canRead(), "file should be readable")
        assertTrue(testFile.canWrite(), "file should be writable")
        assertTrue(testFile.isFile(), "file should be considered a file")
        assertFalse(testFile.isDirectory(), "file should not be considered a directory")
        assertTrue(testFile.delete(), "delete file failed")
    }

    @Test
    fun testCreateTempFileWithinCustomDirAndDelete() {
        val testDir = File("/tmp/testdir").apply { mkdirs() }
        val testFile = Files.createTempFile(prefix = "test", suffix = "all.t", dir = testDir)

        assertEquals("/tmp/testdir/testall.t", testFile.getAbsolutePath())
        assertTrue(testFile.exists(), "file should exist")
        assertTrue(testFile.canRead(), "file should be readable")
        assertTrue(testFile.canWrite(), "file should be writable")
        assertTrue(testFile.isFile(), "file should be considered a file")
        assertFalse(testFile.isDirectory(), "file should not be considered a directory")
        assertTrue(testFile.delete(), "delete file failed")

        assertTrue(testDir.deleteRecursively(), "error while deleting all files in dir")
    }
}
