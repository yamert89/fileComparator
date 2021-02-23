package roslesinforg.porokhin.filecomparator

import roslesinforg.porokhin.filecomparator.service.LineType

class StringResult(private val comparator: FileComparator): ComparingResult<String> {

    override fun get(): String{
        val comparedPairs = comparator.compare()
        val operatedFirstLines = comparedPairs.map { it.first.value.toCharArray().fill(it.first.changedIndexes).toString() }
        val maxLineLength = operatedFirstLines.maxOf { it.length }
      return StringBuilder().apply {
          for ((index, it) in comparedPairs.withIndex()) {
              val value1 = operatedFirstLines[index]
              val value2 = it.second.value
              append(it.first.type.toToken())
              append(value1)
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


    private fun Int.inRanges(ranges: List<Pair<Int, Int>>): Boolean{
        ranges.forEach { if (this >= it.first && this <= it.second ) return true }
        return false
    }

    companion object{
       fun LineType.toToken(): String{
            return when(this){
                LineType.CHANGED -> "<?>"
                LineType.DELETED -> "<->"
                LineType.NEW -> "<+>"
                LineType.BREAK -> "<-------------->"
                LineType.EQUALLY -> "<=>"
            }
       }
    }

}