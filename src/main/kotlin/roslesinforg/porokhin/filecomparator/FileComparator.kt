package roslesinforg.porokhin.filecomparator

import java.io.File
import java.nio.charset.Charset
import org.apache.logging.log4j.kotlin.logger
import roslesinforg.porokhin.filecomparator.service.*

class FileComparator(private val file1: File, private val file2: File, private val charset: Charset = Charset.defaultCharset(), private val visualCapture: Int = 8, private val bufferSize: Int = 100) {
    private val logger = logger()
    private var counter = 0

    fun compare(): MutableList<ComparedPair>{  //fixme - kv 1 vid 8 -> 8b1oc1e bug
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
            when {
                currentLeftIdx > currentRightIdx -> {
                    currentLeftIdx -= currentRightIdx
                    currentRightIdx = 0
                }
                currentLeftIdx < currentRightIdx -> {
                    currentRightIdx -= currentLeftIdx
                    currentLeftIdx = 0
                }
                else -> {
                    currentLeftIdx = 0
                    currentRightIdx = 0
                }
            }
            logger.debug("$counter lines has been read, last = ${block.last()}")
            if (block.isEmpty()) break
            var lineNumber = 0
            while (true){
                val minCurrentIdx = minOf(currentLeftIdx, currentRightIdx)
                if (currentLeftIdx >= block.lastIndex || currentRightIdx >= block.lastIndex) {
                    block = if (currentLeftIdx != currentRightIdx) block.subList(minCurrentIdx, block.size) else mutableListOf()
                    break
                }

                lineNumber = counter
                counter++

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
                            lNum = lineNumber - (minCurrentIdx - startVisualIdx)
                        }
                        minCurrentIdx - visualCapture > lastChangedIdx -> {
                            startVisualIdx = currentLeftIdx - visualCapture
                            lNum = lineNumber - (minCurrentIdx - startVisualIdx)
                            comparedResult.add(ComparedPair.BreakPair)
                        }
                        else -> {
                            startVisualIdx = lastChangedIdx
                            lNum = lineNumber - (minCurrentIdx - startVisualIdx)
                        }
                    }

                    block.subList(startVisualIdx, currentLeftIdx).forEach {
                        comparedResult.add(ComparedPair(lNum++, it.first, LineType.EQUALLY))
                    }
                    newChanging = false
                }

                /*if (first.equalLine(second)){
                    comparedResult.add(ComparedPair(first, LineType.CHANGED, second))
                    continue
                }*/
                val startRegex = "0.+".toRegex()
                val firstIsStartOfVid = first.matches(startRegex)
                val secondIsStartOfVid = second.matches(startRegex)



                if (currentLeftIdx + 2 <= block.lastIndex && currentRightIdx + 2 <= block.lastIndex &&
                    first.equalLine(second) &&  block[currentLeftIdx + 1].first.equalLine(block[currentRightIdx + 1].second) //todo unreliable checking
                ){
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
                    if (/*first.isEndOfVid() || */leftIdx == block.size) {
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
                    if (/*second.isEndOfVid() || */rightIdx == block.size) {
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
                            comparedResult.add(ComparedPair(lineNumber++, ComparedLine(block[j + currentLeftIdx].first, LineType.NEW), ComparedLine.Deleted))
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
                lastChangedIdx = minOf(currentLeftIdx, currentRightIdx)


            }

        }
        if (comparedResult.isNotEmpty()) comparedResult.add(ComparedPair.BreakPair)

        return comparedResult

    }

    fun compare1(): MutableList<ComparedPair>{
        val comparedResult = mutableListOf<ComparedPair>()
        val reader = SomeFileReader(file1, file2, charset, bufferSize)
        var currentLeftBlockIdx = 0
        var currentRightBlockIdx = 0
        var lineNumber = 0
        while (true){
            val blocks = reader.readBlock1()
            val firstBlock = blocks.first[currentLeftBlockIdx]
            val secondBlock = blocks.second[currentRightBlockIdx]

            fun operateTwoEqualBlocks(firstBlock: Block, secondBlock: Block){
                var currentFirstLineIdx = 0
                var currentSecondLineIdx = 0
                while (currentFirstLineIdx < firstBlock.lines.size && currentSecondLineIdx < secondBlock.lines.size){
                    var first = firstBlock.lines[currentFirstLineIdx]
                    var second = secondBlock.lines[currentSecondLineIdx]
                    if (first == second){
                        comparedResult.add(ComparedPair(lineNumber, first, LineType.EQUALLY, second))
                        currentFirstLineIdx++
                        currentSecondLineIdx++
                        continue
                    }
                    var leftIdx = currentFirstLineIdx+ 1
                    var rightIdx = currentSecondLineIdx + 1
                    var firstEqualIdx = 0
                    var secondEqualIdx = 0

                    while (first != second){
                        if (first.isEndOfVid()) {
                            firstEqualIdx = Int.MAX_VALUE
                            break
                        }
                        first = firstBlock.lines[leftIdx++]

                        if (first != second) continue
                        firstEqualIdx = leftIdx
                        break
                    }
                    first = firstBlock.lines[currentFirstLineIdx]
                    while (first != second){
                        if (second.isEndOfVid()) {
                            secondEqualIdx = Int.MAX_VALUE
                            break
                        }
                        second = secondBlock.lines[rightIdx++]
                        if (first != second) continue
                        secondEqualIdx = rightIdx
                        break
                    }
                    second = secondBlock.lines[currentSecondLineIdx]


                    when {
                        firstEqualIdx < secondEqualIdx -> {
                            for (j in 0..firstEqualIdx - currentFirstLineIdx - 2){
                                comparedResult.add(ComparedPair(lineNumber++, ComparedLine(firstBlock.lines[j + currentFirstLineIdx], LineType.NEW), ComparedLine.Deleted))
                            }
                            currentFirstLineIdx = firstEqualIdx - 1
                        }
                        firstEqualIdx > secondEqualIdx -> {
                            for (j in 0..secondEqualIdx - currentSecondLineIdx - 2){
                                comparedResult.add(ComparedPair(lineNumber++, ComparedLine.Deleted, ComparedLine(secondBlock.lines[j + currentSecondLineIdx], LineType.NEW)))
                            }
                            currentSecondLineIdx = secondEqualIdx - 1
                        }
                        firstEqualIdx == secondEqualIdx && secondEqualIdx == Int.MAX_VALUE -> {
                            when {
                                first.isEmpty() -> comparedResult.add(ComparedPair(lineNumber++, ComparedLine.Deleted,
                                    ComparedLine(second, LineType.NEW)))
                                second.isEmpty() -> comparedResult.add(ComparedPair(lineNumber++, ComparedLine(first, LineType.NEW) ,ComparedLine.Deleted))
                                else -> comparedResult.add(ComparedPair(lineNumber++, first, LineType.CHANGED, second, LineType.CHANGED))
                            }
                            currentLeftBlockIdx++
                            currentRightBlockIdx++
                        }
                        else -> {
                            comparedResult.add(ComparedPair(lineNumber++, first, LineType.CHANGED, second, LineType.CHANGED))
                        }
                    }
                }
            }


            if (firstBlock == secondBlock){ operateTwoEqualBlocks(firstBlock, secondBlock)
            } else{
                val firstEqualBlockIdx = blocks.second.indexOf(blocks.second.find { it == firstBlock })
                val secondEqualBlockIdx = blocks.first.indexOf(blocks.first.find { it == secondBlock })
                when{
                    firstEqualBlockIdx > secondEqualBlockIdx -> {
                        val lines = blocks.first[firstEqualBlockIdx].lines
                        for (i in 0..lines.size){
                            comparedResult.add(ComparedPair(lineNumber++, ComparedLine(lines[i], LineType.NEW), ComparedLine.Deleted))
                        }
                        currentLeftBlockIdx = firstEqualBlockIdx + 1
                    }
                    firstEqualBlockIdx < secondEqualBlockIdx -> {
                        val lines = blocks.second[secondEqualBlockIdx].lines
                        for (i in 0..lines.size){
                            comparedResult.add(ComparedPair(lineNumber++, ComparedLine.Deleted, ComparedLine(lines[i], LineType.NEW)))
                        }
                        currentRightBlockIdx = secondEqualBlockIdx + 1
                    }
                    else -> throw IllegalStateException("firstEqualBlockIdx = $firstEqualBlockIdx | secondEqualBlockIdx = $secondEqualBlockIdx")
                }
            }

        }


    }





    private fun String.equalLine(other: String): Boolean{ //todo to refine the algorithm
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

    fun String.isEndOfVid() = this == "?"

    fun Any.debug(messageBefore: String = "", messageAfter: String = ""){
        logger.debug("$messageBefore${toString()}$messageAfter")
    }






}