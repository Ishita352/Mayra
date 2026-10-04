package com.mayra.assistant

import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object SpreadsheetDocumentEngine {
    data class SheetData(val rows: List<List<String>>, val format: String)
    data class Validation(val valid: Boolean, val rows: Int, val columns: Int, val message: String)

    fun readDelimited(input: InputStream, delimiter: Char = ','): SheetData {
        val text = input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        val rows = text.lineSequence().filter { it.isNotBlank() }.map { parseDelimited(it, delimiter) }.toList()
        return SheetData(rows, if (delimiter == '\t') "TSV" else "CSV")
    }

    fun readXlsx(input: InputStream): SheetData {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && (entry.name == "xl/sharedStrings.xml" || entry.name == "xl/worksheets/sheet1.xml")) entries[entry.name] = zip.readBytes()
                entry = zip.nextEntry
            }
        }
        val sheet = entries["xl/worksheets/sheet1.xml"] ?: throw IllegalArgumentException("XLSX-এর প্রথম worksheet পাওয়া যায়নি।")
        val shared = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
        return SheetData(parseWorksheet(sheet, shared), "XLSX")
    }

    fun validate(data: SheetData): Validation {
        if (data.rows.isEmpty()) return Validation(false, 0, 0, "Spreadsheet খালি।")
        val width = data.rows.maxOf { it.size }
        if (width == 0) return Validation(false, data.rows.size, 0, "কোনো column পাওয়া যায়নি।")
        val uneven = data.rows.count { it.size != width }
        return if (uneven == 0) Validation(true, data.rows.size, width, data.format + " validation সফল: " + data.rows.size + " rows × " + width + " columns।")
        else Validation(false, data.rows.size, width, data.format + "-এ " + uneven + "টি row-এর column count আলাদা।")
    }

    fun preview(data: SheetData, maxRows: Int = 10): String = data.rows.take(maxRows).joinToString("\n") { it.joinToString(" | ") }

    private fun parseDelimited(line: String, delimiter: Char): List<String> {
        val out = mutableListOf<String>(); val current = StringBuilder(); var quoted = false; var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> { if (quoted && i + 1 < line.length && line[i + 1] == '"') { current.append('"'); i++ } else quoted = !quoted }
                ch == delimiter && !quoted -> { out += current.toString(); current.setLength(0) }
                else -> current.append(ch)
            }; i++
        }; out += current.toString(); return out
    }

    private fun parseSharedStrings(xml: ByteArray): List<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.inputStream())
        return (0 until doc.getElementsByTagName("si").length).map { index ->
            val si = doc.getElementsByTagName("si").item(index); val nodes = si.childNodes
            buildString { for (i in 0 until nodes.length) { val node = nodes.item(i); if (node.nodeName == "t" || node.nodeName.endsWith(":t")) append(node.textContent); else for (j in 0 until node.childNodes.length) if (node.childNodes.item(j).nodeName == "t" || node.childNodes.item(j).nodeName.endsWith(":t")) append(node.childNodes.item(j).textContent) } }
        }
    }

    private fun parseWorksheet(xml: ByteArray, shared: List<String>): List<List<String>> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.inputStream())
        val cells = doc.getElementsByTagName("c"); val byRow = linkedMapOf<Int, MutableMap<Int, String>>(); var maxRow = -1; var maxCol = -1
        for (i in 0 until cells.length) {
            val cell = cells.item(i); val ref = cell.attributes?.getNamedItem("r")?.nodeValue ?: continue; val match = Regex("([A-Z]+)([0-9]+)").find(ref) ?: continue
            val row = match.groupValues[2].toInt() - 1; val col = columnIndex(match.groupValues[1]); val type = cell.attributes?.getNamedItem("t")?.nodeValue
            var raw = ""; for (j in 0 until cell.childNodes.length) { val n = cell.childNodes.item(j); if (n.nodeName == "v" || n.nodeName.endsWith(":v")) { raw = n.textContent; break } }
            val value = if (type == "s") shared.getOrNull(raw.toIntOrNull() ?: -1) ?: "" else raw
            byRow.getOrPut(row) { mutableMapOf() }[col] = value; maxRow = maxOf(maxRow, row); maxCol = maxOf(maxCol, col)
        }
        if (maxRow < 0) return emptyList()
        return (0..maxRow).map { row -> val rowMap = byRow[row].orEmpty(); (0..maxCol).map { rowMap[it] ?: "" } }
    }

    private fun columnIndex(value: String): Int { var result = 0; for (ch in value) result = result * 26 + (ch - 'A' + 1); return result - 1 }
}