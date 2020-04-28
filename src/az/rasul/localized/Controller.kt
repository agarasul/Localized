package az.rasul.localized

import az.rasul.localized.model.Data
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Pane
import javafx.stage.FileChooser
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class Controller : Initializable {

    private val parsedData = arrayListOf<Data>()

    private var localizableFile: File? = null

    @FXML
    private var mainPane: Pane? = null

    @FXML
    private var filePathTextField: TextField? = null

    @FXML
    private var browseBtn: Button? = null

//    @FXML
//    private var convertBtn: Button? = null

    @FXML
    private var exportBtn: Button? = null

    @FXML
    private var tableView: TableView<Data>? = null

    @FXML
    private var totalCountLabel: Label? = null

    @FXML
    private var progressBar: ProgressBar? = null


    @FXML
    private val androidRdb: RadioButton? = null

    @FXML
    private val iosRdb: RadioButton? = null


    @FXML
    private val csvRdb: RadioButton? = null

    @FXML
    private var radioGroup = ToggleGroup()


    private var type = ""


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        this.browseBtn?.setOnAction(this::onBrowseBtnClicked)
//        this.convertBtn?.setOnAction(this::onConvertBtnClicked)
        this.exportBtn?.setOnAction(this::onExportBtnClicked)



        radioGroup.selectedToggleProperty().addListener { observable, oldValue, newValue ->
//            System.out.println("Selected Radio Button: " + (newValue as RadioButton).id)
            type = (newValue as RadioButton).id
        }

        setupTableView()

//        csvRdb?.toggleGroup = radioGroup
//        androidRdb?.toggleGroup = radioGroup
    }


    private fun setupTableView() {
        val column1: TableColumn<Data, String> = TableColumn("Key")
        column1.cellValueFactory = PropertyValueFactory("key")


        val column2: TableColumn<Data, String> = TableColumn("Value")
        column2.cellValueFactory = PropertyValueFactory("value")

        tableView?.columns?.add(column1)
        tableView?.columns?.add(column2)

        column1.prefWidthProperty().bind(tableView!!.widthProperty()!!.divide(3)) // w * 1/4
        column2.prefWidthProperty().bind(tableView!!.widthProperty()!!.divide(1.3)) // w * 1/4

    }

    private fun onExportBtnClicked(event: ActionEvent) {
        if (parsedData.isEmpty()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error"
            alert.contentText = "Please select the file that you want to convert"
            totalCountLabel?.text = ""
            filePathTextField?.text = ""
            parsedData.clear()
            tableView?.items?.setAll(parsedData)
            localizableFile = null
            alert.show()
        } else {
            val fileChooser = FileChooser()

            fileChooser.title = "Select place to save converted file"
            when (type) {
                "androidRdb" -> {
                    fileChooser.initialFileName = "strings.xml"
                    val fileToSave = fileChooser.showSaveDialog(mainPane?.scene?.window)
                    convertToXml(fileToSave)
                }
                "iosRdb" -> {
                    fileChooser.initialFileName = "Localizable.strings"
                    val fileToSave = fileChooser.showSaveDialog(mainPane?.scene?.window)
                    convertToIOSFormat(fileToSave)
                }
                "csvRdb" -> {
                    fileChooser.initialFileName = "Localized.csv"
                    val fileToSave = fileChooser.showSaveDialog(mainPane?.scene?.window)
                    convertToCSVFormat(fileToSave)
                }
                else -> {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Error"
                    alert.contentText = "Please select the export format!"
                    localizableFile = null
                    alert.show()
                }
            }
        }

    }


    private fun onBrowseBtnClicked(event: ActionEvent) {
        val fileChooser = FileChooser()
        localizableFile = fileChooser.showOpenDialog(mainPane?.scene?.window)
        filePathTextField?.text = localizableFile?.absolutePath


        if (localizableFile != null) {
            val path = localizableFile!!.absolutePath

            var extension = ""
            if (path.contains(".")) {
                extension = path.substring(path.lastIndexOf("."))
            }

            try {

                when (extension) {

                    ".xml" -> {
                        parseFromFromXml()
                    }
                    ".strings" -> {
                        parseFromLocalizableString()
                    }
                    ".csv" -> {
                        parseFromFromCSV()
                    }


                }
                totalCountLabel?.text = "Total count = ${parsedData.size}"
                tableView?.items?.setAll(parsedData)

            } catch (e: Exception) {
                e.printStackTrace()
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error"
                alert.contentText = "Parse error. Please ensure that your file is correct and try again!"
                totalCountLabel?.text = ""
                filePathTextField?.text = ""
                parsedData.clear()
                tableView?.items?.setAll(parsedData)
                localizableFile = null
                alert.show()
            }
        }

//        println("Formatted ---------------------------------------------------------")
//        parsedData.forEach {
//            println(it)
//        }
    }


    private fun convertToXml(file: File) {

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
            element.appendChild(doc.createTextNode(it.value))
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
        showSuccessAlert(filePath = file.absolutePath)
    }


    private fun convertToIOSFormat(file: File) {
        var str = ""
        parsedData.forEach {
            str += "\"${it.key}\" = \"${it.value}\";" + System.lineSeparator()
        }
        file.writeText(str, Charset.defaultCharset())
        showSuccessAlert(filePath = file.absolutePath)
    }

    private fun convertToCSVFormat(file: File) {
        var str = ""
        parsedData.forEach {
            str += "\"${it.key}\",\"${it.value}\"" + System.lineSeparator()
        }
        file.writeText(str, Charset.defaultCharset())
        showSuccessAlert(filePath = file.absolutePath)
    }


    private fun parseFromLocalizableString() {
        parsedData.clear()
        val content = String(Files.readAllBytes(localizableFile!!.toPath()), StandardCharsets.UTF_8)

        val aaa = content.lines()
        aaa.forEachIndexed { index, it ->
            var newLine = it
            val percent = (index.toDouble() + 1) / aaa.count().toDouble()

            progressBar?.progress = percent

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

    }


    private fun parseFromFromXml() {
        parsedData.clear()

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(localizableFile)

        doc.documentElement.normalize()


        val nList = doc.getElementsByTagName("string")


        for (i in 0 until nList.length) {


            val percent = (i.toDouble() + 1) / nList.length.toDouble()

            progressBar?.progress = percent
            val nNode = nList.item(i)


            if (nNode.nodeType == Node.ELEMENT_NODE) {

                val element = nNode as Element


                val key = element.getAttribute("name")
                val value = element.firstChild.textContent

                parsedData.add(Data(key, value))
            }

        }
    }


    private fun parseFromFromCSV() {
        parsedData.clear()
        val content = String(Files.readAllBytes(localizableFile!!.toPath()), StandardCharsets.UTF_8)

        content.lines().forEachIndexed { index, it ->
            val percent = (index.toDouble() + 1) / content.lines().count().toDouble()

            progressBar?.progress = percent

            if (!it.isNullOrBlank()) {
                val keyEndIndex = it.indexOfFirst { it == ',' }

                val key = it.substring(0, keyEndIndex)
                val value = it.substring(keyEndIndex + 1, it.count())

                parsedData.add(Data(key, value))
            }
        }
    }

    private fun showSuccessAlert(filePath: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Success"
        alert.contentText = "Successfully converted. Converted file path : $filePath"
        alert.show()
    }


}