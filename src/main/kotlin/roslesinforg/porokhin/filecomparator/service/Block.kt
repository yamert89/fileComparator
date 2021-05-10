package roslesinforg.porokhin.filecomparator.service

import org.apache.logging.log4j.kotlin.logger
import kotlin.math.log

open class Block(val lines: MutableList<String> = mutableListOf()){
    private val logger = logger()

    fun isEmpty() = this === EmptyBlock
    fun isDeepEmpty()= lines.isEmpty()

    fun deepEquals(other: Block) = lines.containsAll(other.lines) && other.lines.containsAll(lines)

    override fun equals(other: Any?): Boolean{
        try {
            if (this === other) return true
            if (other !is Block) return false
            if (this.isEmpty() && !other.isEmpty() || !isEmpty() && other.isEmpty()) return false
            val kv1 = lines.find { it.match0() }!!.getKv()
            val vid1 = lines.find { it.match1() }!!.getVid()
            val kv2 = other.lines.find { it.match0() }!!.getKv()
            val vid2 = other.lines.find { it.match1() }!!.getVid()
            if (kv1 == kv2 && vid1 == vid2) return true
        }catch (e: Exception){
            logger.error(e)
            logger.error("Attempt equal \n 1 block: \n ${this.lines.joinToString("\n")} \n " +
                    "and 2 block: \n ${(other as Block).lines.joinToString("\n")}")
        }

        return false
    }

    override fun toString(): String {
        return "${lines[0]} | ${lines[1]}"
    }


}

object EmptyBlock: Block()

