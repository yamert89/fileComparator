package roslesinforg.porokhin.filecomparator

class StringComparator(private val line1: String, private val line2: String) {
    private val list1 = mutableListOf<Morpheme>().apply { fill(line1) }
    private val list2 = mutableListOf<Morpheme>().apply { fill(line2) }

    private fun MutableList<Morpheme>.fill(line: String){
        val arr = line.toCharArray()
        for (i in arr.indices){
            val value = if (i + 1 in arr.indices) arr[i + 1] else break
            val prev = arr[i]
            val next = if (i + 2 in arr.indices) arr[i + 2] else Char.MIN_VALUE
            add(Morpheme( prev, value, next))
        }
    }

    private fun compare(): MutableList<String>{
        val equalPieces = mutableListOf<String>()
        var equal = ""
        var id = 0
        for (i in list1.indices){
            for (j in id..list2.lastIndex){
                if (list1[i] == list2[j]) {
                    equal = list1[i].let { "${it.prev}${it.value}${it.next}" }
                    id = j
                    break
                } else {
                    if (equal.isEmpty()) continue
                    equalPieces.add(equal)
                    equal = ""
                    id = 0
                }
            }

        }
        println(equalPieces)
        val buffer = StringBuilder(equalPieces[0])
        val result = mutableListOf<String>()
        for (i in equalPieces.indices){
            if (i + 1 !in equalPieces.indices) break
            val str = equalPieces[i]
            val strNext = equalPieces[i + 1]
            if (str.regionMatches(1, strNext, 0, 2)) {
                buffer.append(strNext.substring(2))
            } else {
                result.add(buffer.toString())
                buffer.clear()
            }

        }
        result.add(buffer.toString())
        return result
    }

    fun indexes(): Pair<List<Pair<Int, Int>>, List<Pair<Int, Int>>>{
        val mergedMorphemes = compare()
        println(mergedMorphemes)
        val idxs1 = mutableListOf<Pair<Int, Int>>()
        val idxs2 = mutableListOf<Pair<Int, Int>>()
        mergedMorphemes.forEach {
            idxs1.add(line1.indexOf(it) to line1.lastIndexOf(it.last()))
            idxs2.add(line2.indexOf(it) to line2.lastIndexOf(it.last()))
        }

        val resIdxs1 = mutableListOf<Pair<Int, Int>>()
        val resIdxs2 = mutableListOf<Pair<Int, Int>>()
        println(idxs1)
        println(idxs2)

        fun MutableList<Pair<Int, Int>>.invert(output: MutableList<Pair<Int, Int>>, lineSize: Int){
            if (this[0].first > 0) output.add(0 to this[0].first - 1)
            for (i in this.indices){
                if (i + 1 > this.lastIndex) break
                output.add(this[i].second + 1 to this[i + 1].first - 1)
            }
            if (get(lastIndex).second < lineSize) output.add(get(lastIndex).second + 1 to lineSize - 1)
        }

        idxs1.invert(resIdxs1, line1.length)
        idxs2.invert(resIdxs2, line2.length)

        return resIdxs1 to resIdxs2
    }

}

class Morpheme(val prev: Char, val value: Char, val next: Char){
    override fun equals(other: Any?): Boolean {
        return if (other !is Morpheme) false else
            value == other.value && prev == other.prev && next == other.next
    }
}