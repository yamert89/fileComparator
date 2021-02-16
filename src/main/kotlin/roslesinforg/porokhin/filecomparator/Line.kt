package roslesinforg.porokhin.filecomparator

class DoubleLine(val prev: ComparedPair?, val value: ComparedPair, val next: ComparedPair? ) {
    override fun equals(other: Any?): Boolean {
        return if (other !is DoubleLine) false else
            value == other.value && prev
    }
}