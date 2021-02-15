class ComparedPair(firstLine: ComparedLine, secondLine: ComparedLine = firstLine){
    var first: ComparedLine = firstLine
        private set

    var second: ComparedLine = secondLine
        private set

    init {
        when(first.type){
            LineType.NEW -> second = ComparedLine.Deleted
            LineType.DELETED -> first = ComparedLine.Deleted
        }
    }
}

open class ComparedLine(var value: String, val type: LineType, val changedIndexes: MutableList<Pair<Int, Int>> = mutableListOf()){

    object Break: ComparedLine(".......", LineType.BREAK)
    object Deleted: ComparedLine("\n", LineType.DELETED)
}

enum class LineType{
    CHANGED,
    NEW,
    DELETED,
    EQUALLY,
    BREAK
}