package az.rasul.localized.localizers

import az.rasul.localized.model.Data
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


object AndroidStrings {
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
     * The method that parse strings.xml
     * @param file strings.xml file
     * @return List with parsed data
     */

    fun convertToXml(file: File, parsedData: List<Data>) {
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

            element.appendChild(doc.createTextNode(replacePlaceHolders(it.value)))
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

    private fun replacePlaceHolders(value: String): String {
        var newString = value
        if (value.contains("%d")) {
            newString = value.replace("%d", "%\$d")
        }
        if (value.contains("%@")) {
            newString = value.replace("%@", "%\$s")
        }
        return newString
    }

}