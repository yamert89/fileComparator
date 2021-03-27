package roslesinforg.porokhin.filecomparator.service

import kotlin.IllegalStateException
import org.apache.logging.log4j.kotlin.logger


class StringComparator(private val line1: String, private val line2: String) {
    private val logger = logger()
    private val list1 = mutableListOf<Morpheme>().apply { fill(line1) }
    private val list2 = mutableListOf<Morpheme>().apply { fill(line2) }

    private fun MutableList<Morpheme>.fill(line: String){
        if (line.isEmpty()) throw IllegalArgumentException("compared string must not be empty")
        val arr = line.toCharArray()
        for (i in arr.indices){
            val value = if (i + 1 in arr.indices) arr[i + 1] else break
            val prev = arr[i]
            val next = if (i + 2 in arr.indices) arr[i + 2] else null
            add(Morpheme( prev, value, next))
        }
    }

    private fun compare(): List<String>{
        logger.debug("inputs: $line1 | $line2")
        val equalPieces = mutableListOf<Pair<String, Boolean>>()
        if (line1.isEmpty() || line2.isEmpty()) return emptyList()

        var id = 0
        for (i in list1.indices){
            var equal = ""
            var mergedable = true
            for (j in id..list2.lastIndex){
                if (list1[i] == list2[j] && i != list1.lastIndex) {
                    equal = list1[i].let { "${it.prev}${it.value}${it.next}" }
                    equalPieces.add(equal to mergedable)
                    id = j + 1
                    break
                } /*else {
                    if (equal.isEmpty()) continue
                    equalPieces.add(equal)
                    equal = ""

                }*/
                mergedable = false

            }
            //id = 0

        }
        logger.debug("equalPieces $equalPieces")
        val buffer = StringBuilder(equalPieces[0].first)
        val result = mutableListOf<String>()
        for (i in equalPieces.indices){
            val str = equalPieces[i].first
            if (i == equalPieces.lastIndex) {
                buffer.append(str)
                break
            }
            val strNext = equalPieces[i + 1].first
            when{
                /*str == strNext -> {
                    buffer.append(str)
                    continue
                }*/
                equalPieces[i + 1].second && str.regionMatches(1, strNext, 0, 2) -> {
                    buffer.append(strNext.substring(2))
                    if (i + 1 == equalPieces.lastIndex) break
                }
                else -> if (buffer.isNotEmpty()) {
                    result.add(buffer.toString())
                    buffer.clear()
                }
            }

        }
        result.add(buffer.toString())
        result.forEach { str ->
            if (!line1.contains(str) || !line2.contains(str)) throw IllegalStateException("Some line not contains merged morpheme")
        }
        return result
    }

    fun indexes(): Pair<List<Pair<Int, Int>>, List<Pair<Int, Int>>>{
        val mergedMorphemes = compare()
        logger.debug("mergedMorphemes $mergedMorphemes")
        if (mergedMorphemes.isEmpty()) return listOf(0 to line1.lastIndex) to listOf(0 to line2.lastIndex)
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
                val start = this[i].second + 1
                var end = this[i + 1].first - 1
                if (end < start) end = start
                output.add( start to end)
            }
            if (get(lastIndex).second < lineSize - 1) output.add(get(lastIndex).second + 1 to lineSize - 1)
        }

        idxs1.invert(resIdxs1, line1.length)
        idxs2.invert(resIdxs2, line2.length)

        resIdxs1.checkValid()
        resIdxs2.checkValid()

        logger.debug("indexes for lines $line1 | $line2 : $resIdxs1 | $resIdxs2")

        return resIdxs1 to resIdxs2
    }

    private fun List<Pair<Int, Int>>.checkValid(){
        forEachIndexed { index, external ->
            forEachIndexed { intIdx, internal ->
                if (index != intIdx) {
                    val range = internal.first..internal.second
                    if (external.first in range || external.second in range) throw IllegalStateException("index pair is invalid") //fixme first = 1.7..3..6:, second = 1.7..3.6:
                }
            }
        }
    }

}

class Morpheme(val prev: Char?, val value: Char, val next: Char?){
    override fun equals(other: Any?): Boolean {
        return if (other !is Morpheme) false else
            value == other.value && prev == other.prev && next == other.next
    }

    override fun toString(): String {
        return "$prev | $value | $next"
    }
}