package roslesinforg.porokhin.filecomparator

import org.apache.logging.log4j.LogManager
import roslesinforg.porokhin.filecomparator.service.ComparedLine
import roslesinforg.porokhin.filecomparator.service.ComparedPair
import roslesinforg.porokhin.filecomparator.service.LineType
import roslesinforg.porokhin.filecomparator.service.SomeFileReader
import java.io.File
import java.nio.charset.Charset

class FileComparator(private val file1: File, private val file2: File, private val charset: Charset = Charset.defaultCharset(), private val visualCapture: Int = 8, private val bufferSize: Int = 100) {
    private val logger = LogManager.getLogger(FileComparator::class)

    fun compare(): MutableList<ComparedPair>{
        val comparedResult = mutableListOf<ComparedPair>()
        val reader = SomeFileReader(file1, file2, charset, bufferSize)
        var block = listOf("1" to "1")
        var newChanging = true

        while (block.isNotEmpty()){
            block = reader.readBlock()
            block.debug()
            var currentLeftIdx = 0
            var currentRightIdx = 0
            for (i in block.indices){
                var first = block[currentLeftIdx].first
                var second = block[currentRightIdx].second
                if (first == second) {
                    newChanging = true
                    currentLeftIdx++
                    currentRightIdx++
                    continue
                }
                if (newChanging){
                    val startVisualIdx = if (i < visualCapture) 0 else i - visualCapture
                    block.subList(startVisualIdx, i - 1).forEach { comparedResult.add(ComparedPair(it.first, LineType.EQUALLY)) }
                    newChanging = false
                }

                /*if (first.equalLine(second)){
                    comparedResult.add(ComparedPair(first, LineType.CHANGED, second))
                    continue
                }*/

                if (i + 1 < block.size && block[i + 1].first == block[i + 1].second){
                    comparedResult.add(ComparedPair(first, LineType.CHANGED, second))
                    continue
                }

                var leftIdx = currentLeftIdx + 1
                var rightIdx = currentRightIdx + 1
                var firstEqualIdx = 0
                var secondEqualIdx = 0

                while (first != second){
                    if (leftIdx + 1 == block.size) {
                        firstEqualIdx = Int.MAX_VALUE 
                        break
                    }
                    first = block[leftIdx++].first
                    if (first != second) continue
                    firstEqualIdx = leftIdx
                    first = block[i].first
                    break
                }
                while (first != second){
                    if (rightIdx + 1 == block.size) {
                        secondEqualIdx = Int.MAX_VALUE
                        break
                    }
                    second = block[rightIdx++].second
                    if (first != second) continue
                    secondEqualIdx = rightIdx
                    second = block[i].second
                    break
                }

                when {
                    firstEqualIdx < secondEqualIdx -> {
                        comparedResult.add(ComparedPair(ComparedLine(first, LineType.EQUALLY), ComparedLine.Deleted))
                        currentLeftIdx++
                    }
                    firstEqualIdx > secondEqualIdx -> {
                        comparedResult.add(ComparedPair("\n", LineType.EQUALLY, second, LineType.NEW))
                        currentRightIdx++
                    }
                    else -> {
                        comparedResult.add(ComparedPair(first, LineType.CHANGED, second, LineType.CHANGED))
                    }
                }


            }



        }

        return comparedResult








        /*val bufferBefore = ArrayBlockingQueue<String>(visualCapture)
        val comparedResult = mutableListOf<roslesinforg.porokhin.filecomparator.service.ComparedPair>()
        while (reader1.ready()){
            val s1 = reader1.readLine()
            val s2 = reader2.readLine()
            if (s1 == s2) {
                bufferBefore.add(s1)
                continue
            }
            bufferBefore.forEach {
                comparedResult.add(roslesinforg.porokhin.filecomparator.service.ComparedPair(roslesinforg.porokhin.filecomparator.service.ComparedLine(it, roslesinforg.porokhin.filecomparator.service.LineType.EQUALLY)))
            }
            if (s1.equalLine(s2)) comparedResult.add(roslesinforg.porokhin.filecomparator.service.ComparedPair(
                roslesinforg.porokhin.filecomparator.service.ComparedLine(s1, roslesinforg.porokhin.filecomparator.service.LineType.CHANGED)
            ))

        }*/

    }



    private fun String.equalLine(other: String): Boolean{

        fun String.equalForOffset(other: String, offset: Int): Int{
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

    fun Any.debug(messageBefore: String = "", messageAfter: String = ""){
        logger.debug("$messageBefore${toString()}$messageAfter")
    }




}