import org.apache.logging.log4j.LogManager
import org.junit.Assert
import org.junit.Test
import roslesinforg.porokhin.filecomparator.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Files
import javax.swing.text.ChangedCharSetException

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
        31.2.1.25.10.Е:
        ?
    """.trimIndent()
    @Test
    fun general(){
        val files = createFiles()
        val file1 = files.first
        val file2 = files.second

        val comparator = FileComparator(file1, file2, 2)
        val list = comparator.compare()
        Assert.assertEquals(3, list.size)
        Assert.assertEquals(1, list.filter { it.second.type == LineType.CHANGED })

    }

    private fun createFiles(): Pair<File, File>{
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
        return file1 to file2
    }

    @Test
    fun fileReader(){
        val files = createFiles()
        val reader = SomeFileReader(files.first, files.second, 100)
        val res = reader.readBlock()
        Assert.assertEquals(input.lines().size, res.size)
    }

    @Test
    fun comparedPair(){
        val input = "3.Б.2.Е КИС.КС:"
        val output = "3.Б.3.Е КИС.Кg"
        val pair = ComparedPair(input, LineType.EQUALLY, output, LineType.CHANGED)
        Assert.assertEquals(2, pair.second.changedIndexes.size)
    }

    @Test
    fun stringComparator(){
        var input = "3.Б.2.Е КИС.КС:"
        var output = "3.Е.2.Е КИС.КС:"
        stringComparatorInv(input, output, 1, 1, 0, 2)
        input = "3.Б.2.Е КИС.КС:"
        output = "3.Б.3.Е КИС.Кg"
        stringComparatorInv(input, output, 2, 2, 4, 7)
    }

    private fun stringComparatorInv(input: String, changed: String, secondSize: Int, fistSize: Int, second0First: Int, second0Second: Int){
        val comparator = StringComparator(input, changed)
        val indexes = comparator.indexes()
        Assert.assertEquals(secondSize, indexes.second.size)
        Assert.assertEquals(fistSize, indexes.first.size)
        Assert.assertEquals(second0First, indexes.second[0].first)
        Assert.assertEquals(second0Second, indexes.second[0].second)
    }
}