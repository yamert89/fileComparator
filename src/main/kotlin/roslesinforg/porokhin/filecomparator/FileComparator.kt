package roslesinforg.porokhin.filecomparator

import java.io.File
import java.nio.charset.Charset
import org.apache.logging.log4j.kotlin.logger
import roslesinforg.porokhin.filecomparator.service.*

class FileComparator(private val file1: File, private val file2: File, private val charset: Charset = Charset.defaultCharset()) {
    private val logger = logger()

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

            fun MutableList<ComparedPair>.addTwoEqualBlocks(firstBlock: Block, secondBlock: Block, runInTheEnd: (MutableList<ComparedPair>) -> Unit = {}){
                val tempResult = mutableListOf<ComparedPair>()
                var currentFirstLineIdx = 0
                var currentSecondLineIdx = 0
                while (currentFirstLineIdx < firstBlock.lines.size && currentSecondLineIdx < secondBlock.lines.size){
                    var first = firstBlock.lines[currentFirstLineIdx]
                    var second = secondBlock.lines[currentSecondLineIdx]
                    if (first == second){
                        tempResult.add(ComparedPair(lineNumber, first, LineType.EQUALLY, second))
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
                                tempResult.add(ComparedPair(lineNumber++, ComparedLine(firstBlock.lines[j + currentFirstLineIdx], LineType.NEW), ComparedLine.Deleted))
                            }
                            currentFirstLineIdx = firstEqualIdx - 1
                        }
                        firstEqualIdx > secondEqualIdx -> {
                            for (j in 0..secondEqualIdx - currentSecondLineIdx - 2){
                                tempResult.add(ComparedPair(lineNumber++, ComparedLine.Deleted, ComparedLine(secondBlock.lines[j + currentSecondLineIdx], LineType.NEW)))
                            }
                            currentSecondLineIdx = secondEqualIdx - 1
                        }
                        firstEqualIdx == secondEqualIdx && secondEqualIdx == Int.MAX_VALUE -> {
                            when {
                                first.isEmpty() -> tempResult.add(ComparedPair(lineNumber++, ComparedLine.Deleted,
                                    ComparedLine(second, LineType.NEW)))
                                second.isEmpty() -> tempResult.add(ComparedPair(lineNumber++, ComparedLine(first, LineType.NEW) ,ComparedLine.Deleted))
                                else -> tempResult.add(ComparedPair(lineNumber++, first, LineType.CHANGED, second, LineType.CHANGED))
                            }
                            currentFirstLineIdx++
                            currentSecondLineIdx++
                        }
                        else -> {
                            tempResult.add(ComparedPair(lineNumber++, first, LineType.CHANGED, second, LineType.CHANGED))
                        }
                    }
                }
                currentLeftBlockIdx++
                currentRightBlockIdx++
                runInTheEnd(tempResult)
                comparedResult.addAll(tempResult)
            }
            var droppedBlock: Block? = null
            var liftedBlock: Block? = null

            var bufferBefore: Block = EmptyBlock
            var needAfter = false

            fun  MutableList<String>.fillComparedResult(firstLine: ComparedLine? = null, secondLine: ComparedLine? = null,
                                                        type: LineType = secondLine?.type ?: LineType.CHANGED){
                val builder: (Int) -> ComparedPair = when{
                    firstLine == null && secondLine == null -> {idx -> ComparedPair(0, ComparedLine(get(idx), LineType.EQUALLY), ComparedLine(get(idx), LineType.EQUALLY)) }
                    firstLine == null -> {idx -> ComparedPair(0, ComparedLine(get(idx), LineType.EQUALLY), secondLine!!)}
                    secondLine == null -> {idx -> ComparedPair(0, firstLine, ComparedLine(get(idx), type)) }
                    else -> throw IllegalStateException("Unknown state")
                }
                for (j in indices) comparedResult.add(builder(j))
            }
            
            while (currentLeftBlockIdx in fBlocks.indices){
                val firstBlock = fBlocks[currentLeftBlockIdx]
                val secondBlock = sBlocks[currentRightBlockIdx]

                fun breakOrNot(){
                    if (needBreak) {
                        comparedResult.add(ComparedPair.BreakPair)
                        needBreak = false
                    }
                }

                fun beforeNeighbourOrNot(){
                    if (needBreak){
                        comparedResult.add(ComparedPair.BreakPair)
                        bufferBefore.lines.fillComparedResult()
                        bufferBefore = EmptyBlock
                        needBreak = false
                    }
                }

                if (firstBlock == secondBlock){
                    if (firstBlock.deepEquals(secondBlock)) {
                        currentLeftBlockIdx++
                        currentRightBlockIdx++
                        needBreak = true
                        bufferBefore = firstBlock
                        if (needAfter) {
                            if (!bufferBefore.deepEquals(firstBlock)) firstBlock.lines.fillComparedResult()
                            needAfter = false
                        }
                        continue
                    }
                    breakOrNot()
                    comparedResult.addTwoEqualBlocks(firstBlock, secondBlock)
                } else{
                    needAfter = true
                    beforeNeighbourOrNot()

                    val fEq = fBlocks.find { it == secondBlock }
                    val sEq = sBlocks.find { it == firstBlock }
                    val firstEqualBlockIdx = if (fEq == null) -1 else fBlocks.indexOf(fEq)
                    val secondEqualBlockIdx = if (sEq == null) -1 else sBlocks.indexOf(sEq)

                    when{
                        firstEqualBlockIdx == secondEqualBlockIdx -> {
                            /*if (firstBlock.getVid().toInt() > secondBlock.getVid().toInt()) {
                                  this check needs for case when input sorting incorrect (may by in future)
                            }*/
                            firstBlock.lines.fillComparedResult(secondLine = ComparedLine.Dropped)
                            secondBlock.lines.fillComparedResult(ComparedLine.Lifted, type = LineType.LIFTED)
                            if(fEq != null && !firstBlock.deepEquals(sEq!!)){
                                comparedResult.addTwoEqualBlocks(firstBlock, sEq){result ->
                                    result.forEach { it.first.shadow = true }
                                }
                            } else sEq!!.lines.fillComparedResult(ComparedLine.Lifted, type = LineType.NEW)
                            currentLeftBlockIdx += 2
                            currentRightBlockIdx += 2
                            continue
                        }
                        secondBlock == droppedBlock -> {
                            secondBlock.lines.fillComparedResult(ComparedLine.Lifted, type = LineType.NEW)
                            droppedBlock = null
                            currentRightBlockIdx++
                        }
                        firstBlock == liftedBlock -> {
                            firstBlock.lines.fillComparedResult(secondLine = ComparedLine.Lifted)
                            liftedBlock = null
                            currentLeftBlockIdx++
                        }
                        firstEqualBlockIdx > 0 && secondEqualBlockIdx == -1 -> {
                            firstBlock.lines.fillComparedResult(secondLine = ComparedLine.Deleted)
                            currentLeftBlockIdx++
                        }
                        secondEqualBlockIdx > 0 && firstEqualBlockIdx == -1 || firstBlock.isEmpty()-> {
                            secondBlock.lines.fillComparedResult(ComparedLine.Deleted, type = LineType.NEW)
                            currentRightBlockIdx++
                        }
                        firstEqualBlockIdx > secondEqualBlockIdx -> {
                            secondBlock.lines.fillComparedResult(ComparedLine.Lifted, type = LineType.NEW)
                            liftedBlock = secondBlock
                            currentRightBlockIdx++
                        }
                        secondEqualBlockIdx > firstEqualBlockIdx -> {
                            firstBlock.lines.fillComparedResult(secondLine = ComparedLine.Dropped)
                            droppedBlock = firstBlock
                            currentLeftBlockIdx++
                        }
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