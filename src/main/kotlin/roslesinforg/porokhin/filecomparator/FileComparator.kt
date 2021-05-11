package roslesinforg.porokhin.filecomparator

import java.io.File
import java.nio.charset.Charset
import org.apache.logging.log4j.kotlin.logger
import roslesinforg.porokhin.filecomparator.service.*

class FileComparator(private val file1: File, private val file2: File, private val charset: Charset = Charset.defaultCharset()) {
    private val logger = logger()
    private var counter = 0

    fun compare(): MutableList<ComparedPair>{
        var needBreak = false
        val comparedResult = mutableListOf<ComparedPair>()
        val reader = SomeFileReader(file1, file2, charset)

        var lineNumber = 0
        while (true){
            val blocks = reader.readBlock()
            var currentLeftBlockIdx = 0
            var currentRightBlockIdx = 0
            logger.debug("Block first size: ${blocks.first.size}")
            val fBlocks = blocks.first
            val sBlocks = blocks.second
            if (fBlocks.isEmpty() && sBlocks.isEmpty()) break

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
                            currentFirstLineIdx++
                            currentSecondLineIdx++
                        }
                        else -> {
                            comparedResult.add(ComparedPair(lineNumber++, first, LineType.CHANGED, second, LineType.CHANGED))
                        }
                    }
                }
                currentLeftBlockIdx++
                currentRightBlockIdx++
            }
            var droppedBlock: Block? = null
            var liftedBlock: Block? = null
            for (i in fBlocks.indices){
                val firstBlock = fBlocks[currentLeftBlockIdx]
                val secondBlock = sBlocks[currentRightBlockIdx]


                if (firstBlock == secondBlock){
                    if (firstBlock.deepEquals(secondBlock)) {
                        currentLeftBlockIdx++
                        currentRightBlockIdx++
                        needBreak = true
                        continue
                    }
                    if (needBreak) {
                        comparedResult.add(ComparedPair.BreakPair)
                        needBreak = false
                    }
                    operateTwoEqualBlocks(firstBlock, secondBlock)
                } else{
                    if (needBreak) {
                        comparedResult.add(ComparedPair.BreakPair)
                        needBreak = false
                    }

                    val fEq = fBlocks.find { it == secondBlock }
                    val sEq = sBlocks.find { it == firstBlock }
                    val firstEqualBlockIdx = if (fEq == null) -1 else fBlocks.indexOf(fEq)
                    val secondEqualBlockIdx = if (sEq == null) -1 else sBlocks.indexOf(sEq)
                    when{//todo add -1 or > 0 variants
                        secondBlock == droppedBlock -> {
                            val lines = secondBlock.lines
                            for (j in lines.indices){
                                comparedResult.add(ComparedPair(0, ComparedLine.Lifted, ComparedLine(lines[j], LineType.NEW)))
                            }
                            droppedBlock = null
                            currentRightBlockIdx++
                        }
                        firstBlock == liftedBlock -> {
                            val lines = firstBlock.lines
                            for (j in lines.indices){
                                comparedResult.add(ComparedPair(0, ComparedLine(lines[j], LineType.EQUALLY), ComparedLine.Lifted))
                            }
                            liftedBlock = null
                            currentLeftBlockIdx++
                        }
                        firstEqualBlockIdx > 0 && secondEqualBlockIdx == -1 -> {
                            val lines = firstBlock.lines
                            for (j in lines.indices){
                                comparedResult.add(ComparedPair(lineNumber++, ComparedLine(lines[j], LineType.NEW), ComparedLine.Deleted))
                            }
                            currentLeftBlockIdx++
                        }
                        secondEqualBlockIdx > 0 && firstEqualBlockIdx == -1 || firstBlock.isEmpty()-> {
                            val lines = secondBlock.lines
                            for (j in lines.indices){
                                comparedResult.add(ComparedPair(lineNumber++, ComparedLine.Deleted, ComparedLine(lines[j], LineType.NEW)))
                            }
                            currentRightBlockIdx++
                        }
                        firstEqualBlockIdx > secondEqualBlockIdx -> {
                            val lines = secondBlock.lines
                            for (j in lines.indices){
                                comparedResult.add(ComparedPair(0, ComparedLine.Lifted, ComparedLine(lines[j], LineType.NEW)))
                            }
                            liftedBlock = secondBlock
                            currentRightBlockIdx++
                        }
                        secondEqualBlockIdx > firstEqualBlockIdx -> {
                            val lines = firstBlock.lines
                            for (j in lines.indices){
                                comparedResult.add(ComparedPair(0, ComparedLine(lines[j], LineType.EQUALLY), ComparedLine.Dropped))
                            }
                            droppedBlock = firstBlock
                            currentLeftBlockIdx++
                        }

                        /*firstEqualBlockIdx == secondEqualBlockIdx -> {

                        }*/
                        else -> throw IllegalStateException("firstEqualBlockIdx = $firstEqualBlockIdx | secondEqualBlockIdx = $secondEqualBlockIdx")
                    }
                }

            }

        }

        return comparedResult


    }

    fun String.isEndOfVid() = this == "?"

    fun Any.debug(messageBefore: String = "", messageAfter: String = ""){
        logger.debug("$messageBefore${toString()}$messageAfter")
    }






}