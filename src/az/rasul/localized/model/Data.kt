package az.rasul.localized.model

data class Data(
        val key: String,
        val value: String
) {
    override fun toString(): String {

        return "Key = $key      Value = $value"
    }
}