package roslesinforg.porokhin.filecomparator

import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.*
import roslesinforg.porokhin.filecomparator.ComparingResult.Companion.toToken
import roslesinforg.porokhin.filecomparator.service.ComparedLine
import roslesinforg.porokhin.filecomparator.service.ComparedPair
import java.math.BigInteger


class MSWordResult(
    private val lines: MutableList<ComparedPair>,
    private val title: String = "",
    private val color: String = "ff0000",
    private val tableColLineNumber: String = "Line",
    private val tableColLine1Type: String = "<>",
    private val tableColLine1 : String = "Before",
    private val tableColLine2Type: String = "<>",
    private val tableColLine2: String = "After"
    ): ComparingResult<XWPFDocument> {
    override fun get(): XWPFDocument {
        val doc = XWPFDocument()
        val head = doc.createHeader(HeaderFooterType.FIRST)
        if (title.isNotEmpty()) doc.createParagraph().apply {
            spacingBeforeLines = 1
            spacingAfterLines = 1
            createRun().apply {
                fontSize = 15
                setText(title)
            }
        }

        val table = doc.createTable(lines.size + 1, 5)
        table.setCellMargins(30, 140, 30, 140)
        table.tableAlignment = TableRowAlign.CENTER
        table.removeBorders()
        var row = table.getRow(0)
            row.apply {
            getCell(0).text = tableColLineNumber
            getCell(1).text = tableColLine1Type
            getCell(2).text = tableColLine1
            getCell(3).text = tableColLine2Type
            getCell(4).text = tableColLine2
        }
        lines.forEachIndexed { idx, pair ->

            fun ComparedLine.highlight(p: XWPFParagraph){
                val buffer = StringBuilder()
                var highlighted: Boolean = false
                value.forEachIndexed { i, char ->
                    if (i.inRanges(changedIndexes)) {
                        if (!highlighted && buffer.isNotEmpty()) {
                            p.createRun().apply {
                                setText(buffer.toString())
                                fontSize = 10
                            }
                            buffer.clear()
                        }
                        highlighted = true
                    } else {
                        if (highlighted && buffer.isNotEmpty()) {
                            p.createRun().apply {
                                setText(buffer.toString())
                                color = this@MSWordResult.color
                                fontSize = 10
                            }
                            buffer.clear()
                        }
                        highlighted = false
                    }
                    buffer.append(char)
                    if (i == value.lastIndex) p.createRun().apply {
                        if (highlighted) color = this@MSWordResult.color
                        setText(buffer.toString())
                        fontSize = 10
                    }
                }
            }

            fun XWPFParagraph.format(){
                spacingBetween = 1.0
                spacingBefore = 0
                spacingAfter = 0
                spacingLineRule = LineSpacingRule.AUTO
                spacingAfterLines = 0
                spacingBeforeLines = 0
            }

            row = table.getRow(idx + 1)
            row.ctRow.addNewTrPr().addNewTrHeight().apply { `val` = BigInteger.valueOf(50) }
            var cell = row.getCell(0)
            var p = cell.getParagraphArray(0)
            p.format()
            var run = p.createRun()
            run.apply {
                setText(pair.lineNumber.toString())
                fontSize = 10
            }
            p = row.getCell(1).getParagraphArray(0)
            p.format()
            p.createRun().apply {
                setText(pair.first.type.toToken())
                fontSize = 10
            }
            cell = row.getCell(2)
            p = cell.getParagraphArray(0)
            p.format()
            pair.first.highlight(p)
            p = row.getCell(3).getParagraphArray(0)
            p.format()
            p.createRun().apply {
                setText(pair.second.type.toToken())
                fontSize = 10
            }
            p = row.getCell(4).getParagraphArray(0)
            p.format()
            pair.second.highlight(p)
        }
        return doc
    }

}