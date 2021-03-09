package roslesinforg.porokhin.filecomparator

import roslesinforg.porokhin.filecomparator.service.ComparedLine
import roslesinforg.porokhin.filecomparator.service.ComparedPair
import roslesinforg.porokhin.filecomparator.service.LineType
import roslesinforg.porokhin.filecomparator.service.SomeFileReader
import java.io.File
import java.nio.charset.Charset
import org.apache.logging.log4j.kotlin.logger

class FileComparator(private val file1: File, private val file2: File, private val charset: Charset = Charset.defaultCharset(), private val visualCapture: Int = 8, private val bufferSize: Int = 100) {
    private val logger = logger()
    private var counter = 0

    fun compare(): MutableList<ComparedPair>{  //fixme - duplicates line in result
        val comparedResult = mutableListOf<ComparedPair>()
        val reader = SomeFileReader(file1, file2, charset, bufferSize)
        var block = mutableListOf("1" to "1")
        var newChanging = true
        var currentLeftIdx = 0
        var currentRightIdx = 0

        while (true){
            val newBlock = reader.readBlock()
            if (newBlock.isEmpty()) break
            block.addAll(newBlock)
            var lastChangedIdx = 0
            if (currentLeftIdx > currentRightIdx) {
                currentLeftIdx -= currentRightIdx
                currentRightIdx = 0
            } else if (currentLeftIdx < currentRightIdx){
                currentRightIdx -= currentLeftIdx
                currentLeftIdx = 0
            }
            logger.debug("$counter lines has been read, last = ${block.last()}")
            if (block.isEmpty()) break

            while (true){
                val lineNumber = 0/*counter + i + 1*/ //todo
                val minCurrentIdx = minOf(currentLeftIdx, currentRightIdx)
                if (currentLeftIdx >= block.lastIndex || currentRightIdx >= block.lastIndex) {
                    block = block.subList(minCurrentIdx, block.lastIndex)
                    break
                }

                var first = block[currentLeftIdx].first
                var second = block[currentRightIdx].second
                if (first == second) {
                    newChanging = true
                    currentLeftIdx++
                    currentRightIdx++
                    continue
                }

                if (newChanging){
                    val startVisualIdx: Int
                    var lNum: Int
                    when{
                        minCurrentIdx < visualCapture -> {
                            startVisualIdx = 0
                            lNum = 1 + counter
                        }
                        minCurrentIdx - visualCapture > lastChangedIdx -> {
                            startVisualIdx = minCurrentIdx - visualCapture
                            lNum = startVisualIdx + counter + 1
                            comparedResult.add(ComparedPair.BreakPair)
                        }
                        else -> {
                            comparedResult.add(ComparedPair.BreakPair)
                            startVisualIdx = lastChangedIdx + 1
                            lNum = startVisualIdx + 1 + counter
                        }
                    }

                    block.subList(startVisualIdx, minCurrentIdx).forEach {
                        comparedResult.add(ComparedPair(lNum++, it.first, LineType.EQUALLY))
                    }
                    newChanging = false
                }

                /*if (first.equalLine(second)){
                    comparedResult.add(ComparedPair(first, LineType.CHANGED, second))
                    continue
                }*/

                if (block[currentLeftIdx + 1].first == block[currentRightIdx + 1].second/* || first.equalLine(second)*/){ //fixme false positives
                    comparedResult.add(ComparedPair(lineNumber, first, LineType.CHANGED, second))
                    currentLeftIdx++
                    currentRightIdx++
                    continue
                }

                var leftIdx = currentLeftIdx + 1
                var rightIdx = currentRightIdx + 1
                var firstEqualIdx = 0
                var secondEqualIdx = 0

                while (first != second){
                    if (leftIdx == block.size) {
                        firstEqualIdx = Int.MAX_VALUE 
                        break
                    }
                    first = block[leftIdx++].first
                    if (first != second) continue
                    firstEqualIdx = leftIdx
                    break
                }
                first = block[currentLeftIdx].first
                while (first != second){
                    if (rightIdx == block.size) {
                        secondEqualIdx = Int.MAX_VALUE
                        break
                    }
                    second = block[rightIdx++].second
                    if (first != second) continue
                    secondEqualIdx = rightIdx
                    break
                }
                second = block[currentRightIdx].second

                when {
                    firstEqualIdx < secondEqualIdx -> {
                        for (j in 0..firstEqualIdx - currentLeftIdx - 2){
                            comparedResult.add(ComparedPair(lineNumber, ComparedLine(block[j + currentLeftIdx].first, LineType.NEW), ComparedLine.Deleted))
                        }
                        currentLeftIdx = firstEqualIdx - 1
                    }
                    firstEqualIdx > secondEqualIdx -> {
                        for (j in 0..secondEqualIdx - currentRightIdx - 2){
                            comparedResult.add(ComparedPair(lineNumber, ComparedLine.Deleted, ComparedLine(block[j + currentRightIdx].second, LineType.NEW)))
                        }
                        currentRightIdx = secondEqualIdx - 1
                    }
                    firstEqualIdx == secondEqualIdx && secondEqualIdx == Int.MAX_VALUE -> {
                        when {
                            first.isEmpty() -> comparedResult.add(ComparedPair(lineNumber, ComparedLine.Deleted,
                                ComparedLine(second, LineType.NEW)))
                            second.isEmpty() -> comparedResult.add(ComparedPair(lineNumber, ComparedLine(first, LineType.NEW) ,ComparedLine.Deleted))
                            else -> comparedResult.add(ComparedPair(lineNumber, first, LineType.CHANGED, second, LineType.CHANGED))
                        }
                        currentLeftIdx++
                        currentRightIdx++
                    }
                    else -> {
                        comparedResult.add(ComparedPair(lineNumber, first, LineType.CHANGED, second, LineType.CHANGED))
                    }
                }
                lastChangedIdx = minCurrentIdx


            }
            counter += block.size

        }

        return comparedResult

    }



    private fun String.equalLine(other: String): Boolean{
        if (this == other) return true
        if (this.length < 3 || other.length < 3) return false
        if (this.length < 4 || other.length < 4){
            val firstStart = substring(0, lastIndex - 1)
            val firstEnd = substring(1, lastIndex)
            val secondStart = other.substring(0, other.lastIndex - 1)
            val secondEnd = other.substring(1, other.lastIndex)
            return firstStart == secondStart || firstStart == secondEnd || firstEnd == secondStart || firstEnd == secondEnd
        }

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
            "$b1  $b2  $b3  $b4  $b5  $b6  $b7".debug()
            return bCounter
        }

        var run = 1
        val firstOffset = when(length){
            4,5 -> 1
            6 -> 2
            else -> 3
        }
        var sum = this.equalForOffset(other, firstOffset)
        if (length > 10 && other.length > 10){
            sum += this.equalForOffset(other, 4)
            run++
        }
        val s = sum.toDouble()
        val res = (s/ (run * 7)) * 100
        res.debug()
        return res >= 50

    }

    fun Any.debug(messageBefore: String = "", messageAfter: String = ""){
        logger.debug("$messageBefore${toString()}$messageAfter")
    }




}