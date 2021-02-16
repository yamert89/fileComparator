package roslesinforg.porokhin.filecomparator

import java.io.File

class FileComparator(private val file1: File, private val file2: File, private val visualCapture: Int = 15, private val bufferSize: Int = 100) {

    fun compare(){
        val comparedResult = mutableListOf<ComparedPair>()
        val reader = SomeFileReader(file1, file2, bufferSize)
        var block = listOf("1" to "1")

        while (block.isNotEmpty()){
            block = reader.readBlock()
            for (i in block.indices){
                var first = block[i].first
                var second = block[i].second
                if (first == second) continue
                while (first != second){
                }
                if (first.equalLine(second)){
                    comparedResult.add(ComparedPair(first, LineType.CHANGED, second))
                }
            }



        }








        /*val bufferBefore = ArrayBlockingQueue<String>(visualCapture)
        val comparedResult = mutableListOf<roslesinforg.porokhin.filecomparator.ComparedPair>()
        while (reader1.ready()){
            val s1 = reader1.readLine()
            val s2 = reader2.readLine()
            if (s1 == s2) {
                bufferBefore.add(s1)
                continue
            }
            bufferBefore.forEach {
                comparedResult.add(roslesinforg.porokhin.filecomparator.ComparedPair(roslesinforg.porokhin.filecomparator.ComparedLine(it, roslesinforg.porokhin.filecomparator.LineType.EQUALLY)))
            }
            if (s1.equalLine(s2)) comparedResult.add(roslesinforg.porokhin.filecomparator.ComparedPair(
                roslesinforg.porokhin.filecomparator.ComparedLine(s1, roslesinforg.porokhin.filecomparator.LineType.CHANGED)
            ))

        }*/
    }



    private fun String.equalLine(other: String): Boolean{

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

        var run = 1
        var sum = this.equalForOffset(other, 3)
        if (length > 10 && other.length > 10){
            sum += this.equalForOffset(other, 4)
            run++
        }
        val s = sum.toDouble()
        val res = (s/ (run * 7)) * 100
        println("comparing $res %")
        return res >= 50

    }




}