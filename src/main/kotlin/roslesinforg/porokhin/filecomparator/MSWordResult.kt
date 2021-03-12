package roslesinforg.porokhin.filecomparator

import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFHeader
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import roslesinforg.porokhin.filecomparator.service.ComparedLine
import roslesinforg.porokhin.filecomparator.service.ComparedPair
import roslesinforg.porokhin.filecomparator.service.LineType


class MSWordResult(private val comparator: FileComparator): ComparingResult<String> {
    override fun get(): String {
        val list = listOf<ComparedPair>(
            ComparedPair(1, ComparedLine("1.0.3.1.3.4:", LineType.CHANGED, mutableListOf(1 to 3)),
                ComparedLine("1.3.4.5.2.1.5:", LineType.CHANGED, mutableListOf(3 to 5))),
            ComparedPair(1, ComparedLine("3.0.3.1.3.5:", LineType.CHANGED, mutableListOf(2 to 6)),
                ComparedLine("4.3.4.5.2.1.5:", LineType.CHANGED, mutableListOf(1 to 4)))
        )
        val doc = XWPFDocument()
        val paragraph = doc.createParagraph()
        val header = doc.createHeader(HeaderFooterType.DEFAULT)
        val table = doc.createTable(list.size, 5)
        list.forEachIndexed { idx, pair ->
            var row = table.getRow(idx)
            var cell = row.getCell(0)
            var p = cell.getParagraphArray(0)
            var run = p.createRun()
            run.setText(pair.lineNumber.toString())
            cell = row.getCell(1)
            p = cell.getParagraphArray(1)
            val buffer = StringBuilder()
            pair.first.value.toCharArray().forEach {

            }
        }
        TODO("Not yet implemented")
    }
}