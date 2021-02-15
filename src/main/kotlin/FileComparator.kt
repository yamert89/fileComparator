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





        }

    }



}