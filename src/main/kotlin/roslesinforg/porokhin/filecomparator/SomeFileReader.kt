package roslesinforg.porokhin.filecomparator

import java.io.*
import java.nio.charset.Charset

class SomeFileReader(private val file1: File, private val file2: File, private val charset: Charset, private val bufferSize: Int) {
    private var reader1 : BufferedReader? = null
    private var reader2 : BufferedReader? = null
    init {

    }

    private fun openReaders(){
        val f1 = if (file1.length() >= file2.length()) file1 else file2
        val f2 = if (f1 == file1) file2 else file1

        reader1 = BufferedReader(InputStreamReader(FileInputStream(f1), charset))
        reader2 = BufferedReader(InputStreamReader(FileInputStream(f2), charset))
    }

    fun readBlock(): List<Pair<String, String>>{
        if (reader1 == null) openReaders()
        val result = mutableListOf<Pair<String, String>>()
        var closed1 = false
        var closed2 = false
        for (i in 0..bufferSize){
            val line1 = if (!closed1) reader1!!.readLine() else null
            val line2 = if (!closed2) reader2!!.readLine() else null
            when{
                line1 == null && line2 != null -> {
                    result.add("" to line2)
                    closed1 = true
                }
                line1 != null && line2 == null -> {
                    result.add(line1 to "")
                    closed2 = true
                }
                line1 == null && line2 == null -> {
                    //close()
                    return result
                }
                else -> result.add(line1!! to line2!!)
            }
        }

        return result
    }
}