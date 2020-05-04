package az.rasul.localized.helpers

import az.rasul.localized.model.Data
import java.io.File
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object Converter {


    fun convert(type: String, file: File, parsedData: List<Data>): Boolean {
        try {
            when (type) {
                "androidRdb" -> {
                    convertToXml(file, parsedData)
                }
                "iosRdb" -> {
                    convertToIOSFormat(file, parsedData)
                }
                "csvRdb" -> {
                    convertToCSVFormat(file, parsedData)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    private fun convertToIOSFormat(file: File, parsedData: List<Data>) {
        var str = ""
        parsedData.forEach {
            str += "\"${it.key}\" = \"${replacePlaceHoldersForIOS(it.value)}\";" + System.lineSeparator()
        }
        file.writeText(str, Charset.defaultCharset())
    }


    private fun convertToCSVFormat(file: File, parsedData: List<Data>) {
        var str = ""
        parsedData.forEach {
            str += "\"${it.key}\",\"${it.value}\"" + System.lineSeparator()
        }
        file.writeText(str, Charset.defaultCharset())
    }


    private fun convertToXml(file: File, parsedData: List<Data>) {
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()


        val doc = docBuilder.newDocument()


        val resourcesElement = doc.createElement("resources")
        doc.appendChild(resourcesElement)
        parsedData.forEach {
            val element = doc.createElement("string")
            val nameAttr = doc.createAttribute("name")
            nameAttr.value = it.key
            element.setAttributeNode(nameAttr)

            if (it.value.contains("'")) {
                it.value = it.value.replace("'", "\\'")
            }

            element.appendChild(doc.createTextNode(replacePlaceHoldersForAndroid(it.value)))
            resourcesElement.appendChild(element)
        }

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        val source = DOMSource(doc)
        val result = StreamResult(file)
        transformer.transform(source, result)
    }

    private fun replacePlaceHoldersForAndroid(value: String): String {
        var newString = value
        if (value.contains("%d")) {
            newString = value.replace("%d", "%\$d")
        }
        if (value.contains("%@")) {
            newString = value.replace("%@", "%\$s")
        }
        return newString
    }

    private fun replacePlaceHoldersForIOS(value: String): String {
        var newString = value
        if (value.contains("%\$d")) {
            newString = value.replace("%\$d", "%d")
        }
        if (value.contains("%\$s")) {
            newString = value.replace("%\$s", "%@")
        }
        return newString
    }

}