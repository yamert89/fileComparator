import org.apache.logging.log4j.LogManager
import org.junit.Assert
import org.junit.Test
import roslesinforg.porokhin.filecomparator.ComparedPair
import roslesinforg.porokhin.filecomparator.FileComparator
import roslesinforg.porokhin.filecomparator.LineType
import roslesinforg.porokhin.filecomparator.StringComparator
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Files

class Unit {
    val logger = LogManager.getLogger(Unit::class)

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
    @Test
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
        val comparator = FileComparator(file1, file2, 2)
        val list = comparator.compare()
        logger.debug("size of list: ${list.size}")
        logger.debug(list.toString())
        Assert.assertEquals(3, list.size)
        Assert.assertEquals(1, list.filter { it.second.type == LineType.CHANGED })

    }

    @Test
    fun comparedPair(){
        val pair = ComparedPair(input, LineType.EQUALLY, output, LineType.CHANGED)
        Assert.assertEquals(2, pair.second.changedIndexes.size)
    }

    @Test
    fun stringComparator(){
        val input = "3.Б.2.Е КИС.КС:"
        val output = "3.Е.2.Е КИС.КС:"
        val comparator = StringComparator(input, output)
        val indexes = comparator.indexes()
        Assert.assertEquals(2, indexes.second.size)
        Assert.assertEquals(2, indexes.first.size)
        Assert.assertEquals(0, indexes.second[0].first)
        Assert.assertEquals(2, indexes.second[0].second)

    }
}