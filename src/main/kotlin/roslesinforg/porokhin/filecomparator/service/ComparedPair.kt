package roslesinforg.porokhin.filecomparator.service

class ComparedPair private constructor(){
    /*private var firstLine: String = ""
    private var secondLine: String = ""*/
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
        val indexes = StringComparator(first.value, second.value).indexes()
        first.changedIndexes.addAll(indexes.first)
        second.changedIndexes.addAll(indexes.second)

    }
}

open class ComparedLine(var value: String, val type: LineType, val changedIndexes: MutableList<Pair<Int, Int>> = mutableListOf()){

    object Break: ComparedLine(".......", LineType.BREAK)
    object Deleted: ComparedLine("\n", LineType.DELETED)
    object NotInitialized: ComparedLine("", LineType.EQUALLY)
}

enum class LineType{
    CHANGED,
    NEW,
    DELETED,
    EQUALLY,
    BREAK
}