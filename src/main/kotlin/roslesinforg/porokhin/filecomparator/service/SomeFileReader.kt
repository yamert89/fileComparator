package roslesinforg.porokhin.filecomparator.service

import java.io.*
import java.nio.charset.Charset

class SomeFileReader(private val file1: File, private val file2: File, private val charset: Charset, private val bufferSize: Int) {
    private var reader1 : BufferedReader? = null
    private var reader2 : BufferedReader? = null
    init {

    }

    private fun openReaders(){
        reader1 = BufferedReader(InputStreamReader(FileInputStream(file1), charset))
        reader2 = BufferedReader(InputStreamReader(FileInputStream(file2), charset))
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
                    return emptyList()
                }
                else -> result.add(line1!!.prepare() to line2!!.prepare())
            }
        }

        return result
    }

    fun readBlock1(): Pair<List<Block>, List<Block>>{
        if (reader1 == null) openReaders()
        val result1 = mutableListOf<Block>()
        val result2 = mutableListOf<Block>()
        var closed1 = false
        var closed2 = false
        var line1: String = ""
        var line2: String = ""
        for (i in 0..bufferSize){ //todo split by kv
            val firstBLock = Block()
            val secondBlock = Block()
            while (line1 != "?"){
                line1 = if (!closed1) reader1!!.readLine() else ""
                firstBLock.lines.add(line1.prepare())
            }
            while (line2 != "?"){
                line2 = if (!closed2) reader2!!.readLine() else ""
                secondBlock.lines.add(line2.prepare())
            }

            when{
                firstBLock.isEmpty() && !secondBlock.isEmpty() -> {
                    result1.add(EmptyBlock)
                    result2.add(secondBlock)
                    closed1 = true
                }
                !firstBLock.isEmpty() && secondBlock.isEmpty() -> {
                    result1.add(firstBLock)
                    result2.add(secondBlock)
                    closed2 = true
                }
                firstBLock.isEmpty() && secondBlock.isEmpty() -> {
                    return emptyList<Block>() to emptyList<Block>()
                }
                else -> {
                    result1.add(firstBLock)
                    result2.add(secondBlock)
                }
            }
        }
        return result1 to result2
    }

    private fun String.prepare() : String{
        if (isEmpty()) throw IllegalStateException("Preparing string is empty")
        return this.replace(".,", ".0,")
    }
}