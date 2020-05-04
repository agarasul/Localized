package az.rasul.localized.helpers

import az.rasul.localized.model.Data
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory

object Parser {


    /**
     * The method that parse strings.xml
     * @param file strings.xml file
     * @return List with parsed data
     */
    fun parseFromFromXml(file: File): List<Data> {
        val parsedData = arrayListOf<Data>()
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)

        doc.documentElement.normalize()


        val nList = doc.getElementsByTagName("string")


        for (i in 0 until nList.length) {
            val nNode = nList.item(i)
            if (nNode.nodeType == Node.ELEMENT_NODE) {

                val element = nNode as Element


                val key = element.getAttribute("name")
                val value = element.firstChild.textContent

                parsedData.add(Data(key, value))
            }

        }
        return parsedData
    }


    /**
     * The method that parse Localizable.strings file
     * @param file Localizable.strings file
     * @return List with parsed data
     */
    fun parseFromLocalizableString(file: File): List<Data> {
        val parsedData = arrayListOf<Data>()
        val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)

        val aaa = content.lines()
        aaa.forEachIndexed { index, it ->
            var newLine = it

            if (newLine.startsWith("//")) {
            } else {

                if (!newLine.isNullOrBlank()) {
                    while (newLine.startsWith(" ")) {
                        newLine = newLine.substring(1)
                    }

                    if (!newLine.endsWith(";")) {
                        newLine += ";"
                    }

                    val equalSignIndex = newLine.indexOfFirst { it == '=' }

                    val keyString = newLine.substring(0, equalSignIndex) // Обрезаем строку от начала до знака =

                    val keyEndIndex = keyString.indexOfLast { it == '"' } // Находим последнюю ковычку в строке

                    var key = newLine.substring(1, keyEndIndex) // Конечная строка (Ключ)

                    while (key.contains(".")) {
                        key = key.replace(".", "")
                    }

                    val valueString = newLine.substring(equalSignIndex, newLine.count())
                    val valueStartIndex = equalSignIndex + valueString.indexOfFirst { it == '"' } + 1 // Находим позицию начала значения
                    val value = newLine.substring(valueStartIndex, newLine.count() - 2)

                    parsedData.add(Data(
                            key = key,
                            value = value
                    ))
                }
            }

        }
        return parsedData
    }


    /**
     * The method that parse CSV file
     * @param file CSV file
     * @return List with parsed data
     */
    fun parseFromFromCSV(file: File): List<Data> {
        val parsedData = arrayListOf<Data>()
        val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)

        content.lines().forEachIndexed { index, it ->

            if (!it.isNullOrBlank()) {
                val keyEndIndex = it.indexOfFirst { it == ',' }

                val key = it.substring(0, keyEndIndex)
                val value = it.substring(keyEndIndex + 1, it.count())

                parsedData.add(Data(key, value))
            }
        }
        return parsedData
    }


}