package roslesinforg.porokhin.filecomparator

class ComparedPair(private val firstLine: String, firstType: LineType, private val secondLine: String = firstLine, secondType: LineType = firstType){
    var first: ComparedLine = ComparedLine(firstLine, firstType)
        private set
        get() {
            prepare()
            return field
        }

    var second: ComparedLine = ComparedLine(secondLine, secondType)
        private set
        get() {
            prepare()
            return field
        }
    private var isPrepared = false

    init {
        when(first.type){
            LineType.NEW -> second = ComparedLine.Deleted
            LineType.DELETED -> first = ComparedLine.Deleted
        }
    }

    private fun prepare(){
        if (isPrepared) return
        val indexes = StringComparator(firstLine, secondLine).indexes()
        first.changedIndexes.addAll(indexes.first)
        second.changedIndexes.addAll(indexes.second)
        isPrepared = true
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