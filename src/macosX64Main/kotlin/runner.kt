import me.archinamon.fileio.File
import me.archinamon.fileio.deleteRecursively
import platform.posix.sleep

fun main(args: Array<String>) = args.forEach { path ->
    val file = File(path)

    if (path.endsWith("/")) {
        println("make directories... ${file.mkdirs()}")
    } else {
        println("make directories... ${file.getParentFile().mkdirs()}")
        println("creating file... ${file.createNewFile()}")
    }

    println(file.toString())

    sleep(5U)
}.also {
    val file = File(args.first())
    print("deleting ${file.getName()}... ${file.deleteRecursively()}")
}