package roslesinforg.porokhin.filecomparator.service

open class Block(val lines: MutableList<String> = mutableListOf()) {

    fun isEmpty() = this == EmptyBlock

    override operator fun equals(other: Any?): Boolean{
        if (other !is Block) return false
        val kv1 = getKv()
        val vid1 = other.getVid()
        val kv2 = getKv()
        val vid2 = other.getVid()
        if (kv1 == kv2 && vid1 == vid2) return true
        return false
    }

    private fun getKv(): String{
        val regex1 = "(0\\.)(\\d{2})(\\..+)".toRegex()
        val m0 = lines.find { it.matches(regex1) }!!
        return regex1.matchEntire(m0)!!.groups[1]!!.value
    }
    private fun getVid(): String{
        val regex2 = "(1\\.)(\\d{1,3})(\\..+)".toRegex()
        val m1 = lines.find { it.matches(regex2) }!!
        return regex2.matchEntire(m1)!!.groups[1]!!.value
    }
}
object EmptyBlock: Block()