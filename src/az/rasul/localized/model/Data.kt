package az.rasul.localized.model

data class Data(
        var key: String,
        var value: String
) {
    override fun toString(): String {

        return "Key = $key      Value = $value"
    }
}