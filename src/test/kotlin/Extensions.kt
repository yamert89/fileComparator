const val m0Reg = "(0\\.)(\\d{1,2})\\.(\\d{1,3})(\\..+)"
const val m1Reg = "(1\\.)(\\d{1,3})(\\..+)"

fun String.match0() = matches(m0Reg.toRegex())

fun String.match1() = matches(m1Reg.toRegex())

fun String.getKv() = if (!match0()) throw IllegalStateException("Not match with m0") else
    m0Reg.toRegex().matchEntire(this)!!.groupValues[3]

fun String.getVid() = if (!match1()) throw IllegalStateException("Not match with m1") else
    m1Reg.toRegex().matchEntire(this)!!.groupValues[2]