package roslesinforg.porokhin.filecomparator

import org.apache.logging.log4j.LogManager


class StringComparator(private val line1: String, private val line2: String) {
    private val logger = LogManager.getLogger(StringComparator::class)
    private val list1 = mutableListOf<Morpheme>().apply { fill(line1) }
    private val list2 = mutableListOf<Morpheme>().apply { fill(line2) }

    private fun MutableList<Morpheme>.fill(line: String){
        val arr = line.toCharArray()
        for (i in arr.indices){
            val value = if (i + 1 in arr.indices) arr[i + 1] else break
            val prev = arr[i]
            val next = if (i + 2 in arr.indices) arr[i + 2] else null
            add(Morpheme( prev, value, next))
        }
    }

    private fun compare(): MutableList<String>{
        logger.debug("inputs: $line1 \n $line2")
        var equalPieces = mutableListOf<String>()
        var equal = ""
        var id = 0
        for (i in list1.indices){
            for (j in id..list2.lastIndex){
                if (list1[i] == list2[j] && i != list1.lastIndex) {
                    equal = list1[i].let { "${it.prev}${it.value}${it.next}" }
                    equalPieces.add(equal)
                    id = j + 1
                    break
                } else {
                    if (equal.isEmpty()) continue
                    equalPieces.add(equal)
                    equal = ""

                }
            }
            id = 0

        }
        equalPieces = equalPieces.toSet().toMutableList()
        logger.debug("equalPieces $equalPieces")
        val buffer = StringBuilder(equalPieces[0])
        val result = mutableListOf<String>()
        for (i in equalPieces.indices){
            if (i + 1 !in equalPieces.indices) break
            val str = equalPieces[i]
            val strNext = equalPieces[i + 1]
            when{
                str == strNext -> {
                    buffer.append(str)
                    continue
                }
                str.regionMatches(1, strNext, 0, 2) -> buffer.append(strNext.substring(2))
                else -> {
                    result.add(buffer.toString())
                    buffer.clear()
                }
            }

        }
        result.add(buffer.toString())
        return result
    }

    fun indexes(): Pair<List<Pair<Int, Int>>, List<Pair<Int, Int>>>{
        val mergedMorphemes = compare()
        logger.debug("mergedMorphemes $mergedMorphemes")
        val idxs1 = mutableListOf<Pair<Int, Int>>()
        val idxs2 = mutableListOf<Pair<Int, Int>>()
        mergedMorphemes.forEach { str ->
            var start = line1.indexOf(str)
            idxs1.add(start to start + str.length - 1)
            start = line2.indexOf(str)
            idxs2.add(start to start + str.length - 1)
        }

        val resIdxs1 = mutableListOf<Pair<Int, Int>>()
        val resIdxs2 = mutableListOf<Pair<Int, Int>>()
        logger.debug("idxs1: $idxs1")
        logger.debug("idxs2: $idxs2")

        fun MutableList<Pair<Int, Int>>.invert(output: MutableList<Pair<Int, Int>>, lineSize: Int){
            if (this[0].first > 0) output.add(0 to this[0].first - 1)
            for (i in this.indices){
                if (i + 1 > this.lastIndex) break
                output.add(this[i].second + 1 to this[i + 1].first - 1)
            }
            if (get(lastIndex).second < lineSize - 1) output.add(get(lastIndex).second + 1 to lineSize - 1)
        }

        idxs1.invert(resIdxs1, line1.length)
        idxs2.invert(resIdxs2, line2.length)

        return resIdxs1 to resIdxs2
    }

}

class Morpheme(val prev: Char?, val value: Char, val next: Char?){
    override fun equals(other: Any?): Boolean {
        return if (other !is Morpheme) false else
            value == other.value && prev == other.prev && next == other.next
    }
}