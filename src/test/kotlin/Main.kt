import roslesinforg.porokhin.filecomparator.service.StringComparator

fun main() {
    indexes()
}

fun stringResult(){

}
fun indexes(){
    val str1 = "abcdefghijlmnopqrstuwxyzdaa"
    val str2 = "45abcdefgh45ijklmnopqrstuwxyz"

    val comparator = StringComparator(str1, str2)


    /*
    *
    * */
    println(comparator.indexes())

}

fun String.indexes(other: String): Pair<List<Pair<Int, Int>>, List<Pair<Int, Int>>>{

    fun compareArrays(arr1: CharArray, arr2: CharArray, list: MutableList<Pair<Int, Int>>){
        val equalPieces = mutableListOf<String>()
        for (i in arr1.indices){
            if (i >= arr2.size) break
            if (arr1[i] == arr2[i]) continue


           /* var secondValue = arr2[i]
            var secIdx = i
            while (arr1[i] != secondValue && secIdx < arr2.size &&
                (i < arr1.size && i < arr2.size && secIdx < arr2.size) && arr1[i + 1] != secondValue  ){
                secondValue = arr2[secIdx++]
            }
            if (secIdx < arr2.lastIndex) list.add(i to secIdx) else list.add(i to i)*/
        }
    }

    val firstList = mutableListOf<Pair<Int, Int>>()
    val secondList = mutableListOf<Pair<Int, Int>>()
    var arr1: CharArray? = null
    var arr2: CharArray? = null
    if (length >= other.length) {
        arr1 = this.toCharArray()
        arr2 = other.toCharArray()
    } else {
        arr2 = this.toCharArray()
        arr1 = other.toCharArray()
    }

    compareArrays(arr1, arr2, firstList)
    compareArrays(arr2, arr1, secondList)
    return firstList to secondList
}




fun equalTest(){
    val str1 = "hjdfg5464fdfghrfh45hgfdhd"
    val str2 = "ghjdfg5464fdhrfh45hgfdhdf"
    println(str1.equalLine(str2))
}

fun String.equalLine(other: String): Boolean{
    var run = 1
    var sum = this.equalForOffset(other, 3)
    if (length > 10 && other.length > 10){
        sum += this.equalForOffset(other, 4)
        run++
    }
    val s = sum.toDouble()
    val res = (s/ (run * 7)) * 100
    println("comparing ${res} %")
    return res >= 50

}
fun String.equalForOffset(other: String, offset: Int): Int{
    val start = substring(0, offset)
    val end = substring(lastIndex - offset, length)
    val idx = lastIndex/2
    val middle = substring(idx, offset + idx)
    val piece1 = substring(3, offset + 3)
    val arr1 = this.toCharArray()
    val arr2 = other.toCharArray()
    val b1 = other.contains(start)
    val b2 = other.contains(end)
    val b3 = other.contains(middle)
    val b4 = arr1.subtract(arr2.asIterable()).size < length / 2
    val b5 = other.startsWith(start)
    val b6 = other.endsWith(end)
    val b7 = other.contains(piece1)
    var bCounter = 0
    if (b1) bCounter++
    if (b2) bCounter++
    if (b3) bCounter++
    if (b4) bCounter++
    if (b5) bCounter++
    if (b6) bCounter++
    if (b7) bCounter++
    println("$b1  $b2  $b3  $b4  $b5  $b6  $b7")
    return bCounter
}
