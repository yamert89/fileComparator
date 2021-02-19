package roslesinforg.porokhin.filecomparator

class StringResult(private val comparator: FileComparator): ComparingResult<String> {

    override fun get(): String{
        val comparedPairs = comparator.compare()
        val maxLineLength = comparedPairs.maxOf { it.first.value.length }
      return StringBuilder().apply {
          comparedPairs.forEach {

              val value1 = it.first.value
              val value2 = it.second.value
              val selected1 = mutableListOf<Char>()
              val selected2 = mutableListOf<String>()
              append(it.first.type.toToken())
              append(value1.toCharArray().fill(it.first.changedIndexes))
              append(" ".repeat(maxLineLength - value1.length))
              append("      ")
              append(it.second.type.toToken())
              append(value2.toCharArray().fill(it.second.changedIndexes))
              append("\n")

          }
      }.toString()

    }

    private fun CharArray.fill(ranges: List<Pair<Int, Int>>): StringBuilder{
        var inSelected = false
        return StringBuilder().apply {
            for (i in this@fill.indices){
                if (i.inRanges(ranges)){
                    if (!inSelected){
                        inSelected = true
                        append("[")
                    }
                } else {
                    if (inSelected) {
                        inSelected = false
                        append("]")
                    }
                }
                append(this@fill[i])
            }
        }

    }

    private fun LineType.toToken(): String{
        return when(this){
            LineType.CHANGED -> "<?>"
            LineType.DELETED -> "<->"
            LineType.NEW -> "<+>"
            LineType.BREAK -> "<-------------->"
            LineType.EQUALLY -> "<=>"
        }
    }

    private fun Int.inRanges(ranges: List<Pair<Int, Int>>): Boolean{
        ranges.forEach { if (this >= it.first && this <= it.second ) return true }
        return false
    }

}