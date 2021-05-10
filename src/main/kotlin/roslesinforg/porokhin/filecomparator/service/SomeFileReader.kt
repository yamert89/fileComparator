package roslesinforg.porokhin.filecomparator.service

import org.apache.logging.log4j.kotlin.logger
import java.io.*
import java.nio.charset.Charset

class SomeFileReader(private val file1: File, private val file2: File, private val charset: Charset, private val bufferSize: Int) {
    private var reader1 : BufferedReader? = null
    private var reader2 : BufferedReader? = null
    private var oldStrings: Pair<String, String>? = null
    private var counter = 0
    var closed1 = false
    var closed2 = false
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
        if (closed1 && closed2) return emptyList<Block>() to emptyList<Block>()

        val result1 = mutableListOf<Block>()
        val result2 = mutableListOf<Block>()


        var nextKvInLeft = false
        var nextKvInRight = false
        var kv  = ""
        while (true){
            var line1: String? = ""
            var line2: String? = ""
            val firstBLock = Block()
            val secondBlock = Block()
            if (oldStrings?.let { it.first.isNotEmpty() && it.second.isNotEmpty() } == true) {
                firstBLock.lines.add(oldStrings!!.first)
                secondBlock.lines.add(oldStrings!!.second) //fixme empty on deleted
                oldStrings = null
            }
            while (line1 != "?" && !nextKvInLeft){
                line1 = if (!closed1) reader1!!.readLine() else null
                if (line1 == null) {
                    closed1 = true
                    break
                }
                if (line1.match0() && kv.isNotEmpty() && line1.getKv() != kv) {
                    nextKvInLeft = true
                    oldStrings = line1 to (oldStrings?.second ?: "")
                    break
                }
                if (kv.isEmpty() && line1.match0()) kv = line1.getKv()
                firstBLock.lines.add(line1.prepare())
            }
            while (line2 != "?" && !nextKvInRight){
                line2 = if (!closed2) reader2!!.readLine() else null
                if (line2 == null) {
                    closed2 = true
                    break
                }
                if (line2.match0() && kv.isNotEmpty() && line2.getKv() != kv){
                    nextKvInRight = true
                    oldStrings = (oldStrings?.first ?: "") to line2
                    break
                }
                if (kv.isEmpty() && line2.match0()) kv = line2.getKv()
                secondBlock.lines.add(line2.prepare())
            }
            if (closed1 && closed2){
                return result1 to result2
            }
            if (nextKvInLeft && nextKvInRight){
                return result1 to result2
            }

            when{

                firstBLock.isDeepEmpty() && !secondBlock.isDeepEmpty() -> {
                    result1.add(EmptyBlock)
                    result2.add(secondBlock)
                    closed1 = true
                }
                !firstBLock.isDeepEmpty() && secondBlock.isDeepEmpty() -> {
                    result1.add(firstBLock)
                    result2.add(EmptyBlock)
                    closed2 = true
                }
                firstBLock.isDeepEmpty() && secondBlock.isDeepEmpty() -> { //todo ?
                    return emptyList<Block>() to emptyList<Block>()
                }
                else -> {
                    result1.add(firstBLock)
                    result2.add(secondBlock)
                }
            }
            //println(counter++)
        }
       // return result1 to result2
    }

    private fun String.prepare() : String{
        if (isEmpty()) throw IllegalStateException("Preparing string is empty")
        return this.replace(".,", ".0,")
    }
}