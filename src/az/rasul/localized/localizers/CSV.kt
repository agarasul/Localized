package az.rasul.localized.localizers

import az.rasul.localized.model.Data
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object CSV {



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



    fun convert(file: File, parsedData: List<Data>) {
        var str = ""
        parsedData.forEach {
            str += "\"${it.key}\",\"${it.value}\"" + System.lineSeparator()
        }
        file.writeText(str, Charset.defaultCharset())
    }




}