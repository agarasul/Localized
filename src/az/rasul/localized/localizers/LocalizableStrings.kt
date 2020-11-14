package az.rasul.localized.localizers

import az.rasul.localized.model.Data
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.regex.Pattern


object LocalizableStrings {


    const val PATTERN = "[%][\\d]?[0-9]?[\$][a-z]"

    /**
     * The method that parse Localizable.strings file
     * @param file Localizable.strings file
     * @return List with parsed data
     */
    fun parseFromString(file: File): List<Data> {
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

//                    while (key.contains(".")) {
//                        key = key.replace(".", "")
//                    }

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



    fun convertToIOSFormat(file: File, parsedData: List<Data>) {
        var str = ""
        parsedData.forEach {
            str += "\"${it.key}\" = \"${replacePlaceHolders(it.value)}\";" + System.lineSeparator()
        }
        file.writeText(str, Charset.defaultCharset())
    }





    // [%][\d]?[0-9]?[$][a-z]   IOS Pattern String interpolation



    private fun replacePlaceHolders(value: String): String {
        var newString = value
        if (value.contains("%\$d")) {
            newString = value.replace("%\$d", "%d")
        }
        if (value.contains("%\$s")) {
            newString = value.replace("%\$s", "%@")
        }
        return newString
    }


    /**
     * Converts placeholders from strings.xml to Localizable.strings format
     * @param value Value from strings.xml
     */
    private fun replacePlaceHolder(value : String) : String {
        val m2 = Pattern.compile(PATTERN).matcher(value)
        while (m2.find()) {
            val placeholder = m2.group()



            break
        }
    }

}


