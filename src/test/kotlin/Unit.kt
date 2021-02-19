import org.junit.Assert
import org.junit.Test
import roslesinforg.porokhin.filecomparator.*
import roslesinforg.porokhin.filecomparator.service.ComparedPair
import roslesinforg.porokhin.filecomparator.service.LineType
import roslesinforg.porokhin.filecomparator.service.SomeFileReader
import roslesinforg.porokhin.filecomparator.service.StringComparator
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files

class Unit {

    val outPath = "C:/stringResult"

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
    fun stringResult(){
        val files = createFiles()
        val comparator = FileComparator(files.first, files.second)
        val stringResult = StringResult(comparator)
        val out = File(outPath)
        val result = stringResult.get()
        FileWriter(out).apply {
            write(result)
            close()
        }
        Assert.assertEquals(false, result.isEmpty())
    }
    @Test
    fun fileComparator(){
        val files = createFiles()
        val file1 = files.first
        val file2 = files.second

        val comparator = FileComparator(file1, file2, Charset.defaultCharset(), 2)
        val list = comparator.compare()
        Assert.assertEquals(3, list.size)
        Assert.assertEquals(2, list.filter { it.second.type == LineType.CHANGED }.size)

    }

    private fun createFiles(): Pair<File, File>{
        val file1 = Files.createTempFile("", "").toFile()
        val file2 = Files.createTempFile("", "").toFile()
       OutputStreamWriter(FileOutputStream(file1), Charset.defaultCharset()).apply {
            write(input)
            flush()
            close()
        }
        OutputStreamWriter(FileOutputStream(file2), Charset.defaultCharset()).apply{
            write(output)
            flush()
            close()
        }
        return File(file1.path) to File(file2.path)
    }

    @Test
    fun fileReader(){
        val files = createFiles()
        val reader = SomeFileReader(files.first, files.second, Charsets.UTF_8,100)
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
        stringComparatorInv(input, output, 1, 1, 0, 2, 0, 2)
        input = "3.Б.2.Е КИС.КС:"
        output = "3.Б.3.Е КИС.Кg"
        stringComparatorInv(input, output, 2, 2, 4, 7, 4, 7)
    }

    private fun stringComparatorInv(input: String, changed: String, secondSize: Int, fistSize: Int, first0First: Int,
                                    first0Second: Int, second0First: Int, second0Second: Int){
        val comparator = StringComparator(input, changed)
        val indexes = comparator.indexes()
        Assert.assertEquals(secondSize, indexes.second.size)
        Assert.assertEquals(fistSize, indexes.first.size)
        Assert.assertEquals(first0First, indexes.second[0].first)
        Assert.assertEquals(first0Second, indexes.second[0].second)
        Assert.assertEquals(second0First, indexes.first[0].first)
        Assert.assertEquals(second0Second, indexes.first[0].second)
    }
}