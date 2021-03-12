package roslesinforg.porokhin.filecomparator

import roslesinforg.porokhin.filecomparator.service.LineType

interface ComparingResult<T> {
    fun get(): T
    fun Int.inRanges(ranges: List<Pair<Int, Int>>): Boolean{
        ranges.forEach { if (this >= it.first && this <= it.second ) return true }
        return false
    }
    companion object{
        fun LineType.toToken(): String{
            return when(this){
                LineType.CHANGED -> "<?>"
                LineType.DELETED -> "<->"
                LineType.NEW -> "<+>"
                LineType.BREAK -> "---"
                LineType.EQUALLY -> "<=>"
            }
        }
    }
}