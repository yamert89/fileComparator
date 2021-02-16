package roslesinforg.porokhin.filecomparator

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class SomeFileReader(file1: File, file2: File, private val bufferSize: Int) {
    private var reader1 : BufferedReader
    private var reader2 : BufferedReader
    init {
        val f1 = if (file1.length() >= file2.length()) file1 else file2
        val f2 = if (f1 == file1) file2 else file1
        reader1 = BufferedReader(FileReader(f1))
        reader2 = BufferedReader(FileReader(f2))
    }

    fun readBlock(): List<Pair<String, String>>{
        val result = mutableListOf<Pair<String, String>>()
        for (i in 0..bufferSize){
            when{
                !reader1.ready() && reader2.ready() -> result.add("" to reader2.readLine())
                reader1.ready() && !reader2.ready() -> result.add(reader1.readLine() to "")
                !reader1.ready() && !reader2.ready() -> {
                    close()
                    return emptyList()
                }
                else -> result.add(reader1.readLine() to reader2.readLine())
            }
        }

        return result
    }

    private fun close(){
        reader1.close()
        reader2.close()
    }
}