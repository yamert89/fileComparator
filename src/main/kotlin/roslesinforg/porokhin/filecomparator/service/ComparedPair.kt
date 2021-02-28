package roslesinforg.porokhin.filecomparator.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.kotlin.logger
import kotlin.Exception

open class ComparedPair private constructor(){
    /*private var firstLine: String = ""
    private var secondLine: String = ""*/
    private val logger = logger()
    var first: ComparedLine = ComparedLine.NotInitialized
        private set
        get() {
            prepare()
            return field
        }

    var second: ComparedLine = ComparedLine.NotInitialized
        private set
        get() {
            prepare()
            return field
        }
    private var isPrepared = false

    constructor(firstLine: String, firstType: LineType, secondLine: String = firstLine, secondType: LineType = firstType): this(){
        first = ComparedLine(firstLine, firstType)
        second = ComparedLine(secondLine, secondType)
    }
    constructor(firstLine: ComparedLine, secondLine: ComparedLine): this(){
        first = firstLine
        second = secondLine
    }

    private fun prepare(){
        if (isPrepared) return
        isPrepared = true
        if (first.value == second.value || first == ComparedLine.Deleted || second == ComparedLine.Deleted) return
        try{
            val indexes = StringComparator(first.value, second.value).indexes()
            first.changedIndexes.addAll(indexes.first)
            second.changedIndexes.addAll(indexes.second)
        }catch (e: Exception){
            logger.error("Exception in string comparing first = ${first.value}, second = ${second.value}")
            logger.error(e)
        }


    }

    override fun toString(): String {
        return "$first $second"
    }

    object BreakPair: ComparedPair(ComparedLine.Break, ComparedLine.Break)

}

open class ComparedLine(var value: String, val type: LineType, val changedIndexes: MutableList<Pair<Int, Int>> = mutableListOf()){

    object Break: ComparedLine("", LineType.BREAK)
    object Deleted: ComparedLine("\n", LineType.DELETED)
    object NotInitialized: ComparedLine("", LineType.EQUALLY)

    override fun toString(): String {
        return value
    }
}

enum class LineType{
    CHANGED,
    NEW,
    DELETED,
    EQUALLY,
    BREAK
}