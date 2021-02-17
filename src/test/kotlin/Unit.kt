import roslesinforg.porokhin.filecomparator.FileComparator
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Files

class Unit {

    val input = """
        11111103.09?
        0.92.1.205:
        1.1..3:
        3.Б.2.Е КИС.КС:
        10.1.6.Б.450.17.16...90..230:
        10.1.2.ОС..18.18:
        10.1.2.Е.80.17.18:
        31.2.2.25.10.Е:
        13.20.2.2:
        11.1.2.3.4.5.6.7.8:
        23.3.9.1:
        ?
        0.92.1.205:
        1.2..3:
        3.Е.4.Е ЧЕР.ЧС:
        10.1.9.Е.120.18.20.1..60..180:
        10.1.1.Б..19.22.3:
        31.2.1,5.25.10.Е:
        ?
        0.28.1.205:
        1.3..3..6:
        3.Б.2.Е КИС.КС:
        10.1.6.Б.50.17.16...90..230:
        10.1.2.ОС..18.18:
        10.1.2.Е.80.17.18:
        31.2.2.25.10.Е:
        ?
        0.28.1.205:
        1.4..3..45:
        3.Б.2.Е КИС.КС:
        10.1.6.Б.50.17.16...90..230:
        10.1.2.ОС..18.18:
        10.1.2.Е.80.17.18:
        31.2.2.25.10.Е:
        ?
    """.trimIndent()

    val output = """
        11111103.09?
        0.92.1.205:
        1.1..3:
        3.Б.2.Е КИС.КС:
        10.1.6.Б.450.17.16...90..230:
        10.1.2.ОС..18.18:
        10.1.2.Е.80.17.18:
        31.2.2.25.10.Е:
        13.20.2.2:
        11.1.2.3.4.5.6.7.8:
        23.3.9.1:
        ?
        0.92.1.205:
        1.2..3:
        3.Е.4.Е ЧЕР.ЧС:
        10.1.9.Е.120.18.20.1..60..180:
        10.1.1.Б..19.22.3:
        31.2.1,5.25.10.Е:
        ?
        0.28.1.205:
        1.3..3..6:
        3.Б.2.Е КИС.КС:
        10.1.6.Б.50.17.16...90..230:
        10.1.2.ОС..18.18:
        10.1.2.Е.80.17.18:
        31.2.2.25.10.Е:
        ?
        0.28.1.205:
        1.4..3..45:
        3.Б.2.Е КИС.КС:
        10.1.6.Б.50.17.16...90..230:
        10.1.2.ОС..18.18:
        10.1.2.Е.80.17.18:
        31.2.2.25.10.Е:
        ?
    """.trimIndent()

    fun general(){
        val file1 = Files.createTempFile("0000", "0000").toFile()
        val file2 = Files.createTempFile("0001", "0001").toFile()
        FileOutputStream(file1).apply {
            write(input.toByteArray())
            flush()
            close()
        }
        FileOutputStream(file2).apply {
            write(output.toByteArray())
            flush()
            close()
        }
        val comparator = FileComparator(file1, file2)
    }
}