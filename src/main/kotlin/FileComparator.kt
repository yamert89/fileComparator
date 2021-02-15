import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class FileComparator(private val file1: File, private val file2: File, private val visualCapture: Int = 15) {

    fun compare(){
        val f1 = if (file1.length() >= file2.length()) file1 else file2
        val f2 = if (f1 == file1) file2 else file1
        val reader1 = BufferedReader(FileReader(f1))
        val reader2 = BufferedReader(FileReader(f2))
        val bufferBefore = ArrayBlockingQueue<String>(visualCapture)
        val comparedResult = mutableListOf<ComparedPair>()
        while (reader1.ready()){
            val s1 = reader1.readLine()
            val s2 = reader2.readLine()
            if (s1 == s2) {
                bufferBefore.add(s1)
                continue
            }
            bufferBefore.forEach {
                comparedResult.add(ComparedPair(ComparedLine(it, LineType.EQUALLY)))
            }
            if (s1.equalLine(s2)) comparedResult.add(ComparedPair(
                ComparedLine(s1, LineType.CHANGED)
            ))





        }

    }

    private fun String.equalLine(other: String): Boolean{
        var run = 1
        var sum = this.equalForOffset(other, 3)
        if (length > 10 && other.length > 10){
            sum += this.equalForOffset(other, 4)
            run++
        }
        val s = sum.toDouble()
        val res = (s/ (run * 7)) * 100
        println("comparing $res %")
        return res >= 50

    }
    private fun String.equalForOffset(other: String, offset: Int): Int{
        val start = substring(0, offset)
        val end = substring(lastIndex - offset, length)
        val idx = lastIndex/2
        val middle = substring(idx, offset + idx)
        val piece1 = substring(3, offset + 3)
        val arr1 = this.toCharArray()
        val arr2 = other.toCharArray()
        val b1 = other.contains(start)
        val b2 = other.contains(end)
        val b3 = other.contains(middle)
        val b4 = arr1.subtract(arr2.asIterable()).size < length / 2
        val b5 = other.startsWith(start)
        val b6 = other.endsWith(end)
        val b7 = other.contains(piece1)
        var bCounter = 0
        if (b1) bCounter++
        if (b2) bCounter++
        if (b3) bCounter++
        if (b4) bCounter++
        if (b5) bCounter++
        if (b6) bCounter++
        if (b7) bCounter++
        println("$b1  $b2  $b3  $b4  $b5  $b6  $b7")
        return bCounter
    }



}