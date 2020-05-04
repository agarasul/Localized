package az.rasul.localized.controller

import az.rasul.localized.helpers.Converter
import az.rasul.localized.helpers.Parser
import az.rasul.localized.model.Data
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Pane
import javafx.stage.FileChooser
import javafx.util.Callback
import java.io.File
import java.net.URL
import java.util.*


class Controller : Initializable {

//    private val parsedData = arrayListOf<Data>()


    private val parsedData = FXCollections.observableArrayList<Data>()

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
//        tableView?.isEditable = true

        val column1: TableColumn<Data, String> = TableColumn("Key")
        column1.cellValueFactory = PropertyValueFactory("key")

        val column2: TableColumn<Data, String> = TableColumn("Value")
        column2.cellValueFactory = PropertyValueFactory("value")


        tableView?.columns?.addAll(column1, column2)

        column1.prefWidthProperty().bind(tableView!!.widthProperty()!!.divide(3)) // w * 1/4
        column2.prefWidthProperty().bind(tableView!!.widthProperty()!!.divide(1.3)) // w * 1/4

        tableView?.items = parsedData
    }


    private fun onExportBtnClicked(event: ActionEvent) {
        if (parsedData.isEmpty()) {
            totalCountLabel?.text = ""
            filePathTextField?.text = ""
            parsedData.clear()
            tableView?.items?.setAll(parsedData)
            localizableFile = null
            showErrorAlert(message = "Please select the file that you want to convert")
        } else {
            val fileChooser = FileChooser()
            fileChooser.title = "Select place to save converted file"
            val fileToSave: File?

            val window = mainPane?.scene?.window

            when (type) {
                "androidRdb" -> {
                    fileChooser.initialFileName = "strings.xml"
                    fileToSave = fileChooser.showSaveDialog(window)
                }
                "iosRdb" -> {
                    fileChooser.initialFileName = "Localizable.strings"
                    fileToSave = fileChooser.showSaveDialog(window)
                }
                "csvRdb" -> {
                    fileChooser.initialFileName = "Localized.csv"
                    fileToSave = fileChooser.showSaveDialog(window)
                }
                else -> {
                    fileToSave = null
                    localizableFile = null
                    showErrorAlert(message = "Please select the export format!")
                }
            }
            if (fileToSave != null) {
                val isSuccess = Converter.convert(type, fileToSave, parsedData)
                if (isSuccess) {
                    showSuccessAlert(filePath = fileToSave.absolutePath)
                } else {
                    showErrorAlert()
                }

            }
        }

    }


    private fun onBrowseBtnClicked(event: ActionEvent) {
        val fileChooser = FileChooser()
        val extFilter = FileChooser.ExtensionFilter("Apple (*.strings), Android (*.xml), .csv", "*.xml", "*.csv", "*.strings")
        fileChooser.extensionFilters.add(extFilter)

        localizableFile = fileChooser.showOpenDialog(mainPane?.scene?.window)
        filePathTextField?.text = localizableFile?.absolutePath

        if (localizableFile != null) {
            val path = localizableFile!!.absolutePath

            var extension = ""
            if (path.contains(".")) {
                extension = path.substring(path.lastIndexOf("."))
            }

            try {
                parsedData.clear()

                when (extension) {
                    ".xml" -> {
                        parsedData.addAll(Parser.parseFromFromXml(localizableFile!!))
                    }
                    ".strings" -> {
                        parsedData.addAll(Parser.parseFromLocalizableString(localizableFile!!))
                    }
                    ".csv" -> {
                        parsedData.addAll(Parser.parseFromFromCSV(localizableFile!!))
                    }


                }
                totalCountLabel?.text = "Total count = ${parsedData.size}"
//                tableView?.items?.setAll(parsedData)

            } catch (e: Exception) {
                e.printStackTrace()
                totalCountLabel?.text = ""
                filePathTextField?.text = ""
                parsedData.clear()
                tableView?.items?.setAll(parsedData)
                localizableFile = null
                showErrorAlert(message = "Parse error. Please ensure that your file is correct and try again!")
            }
        }

//        println("Formatted ---------------------------------------------------------")
//        parsedData.forEach {
//            println(it)
//        }
    }


    private fun showSuccessAlert(filePath: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Success"
        alert.contentText = "Successfully converted. Converted file path:\n$filePath"
        alert.show()
    }

    private fun showErrorAlert(message: String = "Something went wrong. Please try again") {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Error"
        alert.contentText = message
        alert.show()
    }


}