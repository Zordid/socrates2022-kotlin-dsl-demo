/**
 * Olaf Gottschalk, SoCraTes 2022
 *
 * Twitter @coder_ogo
 */

import utils.JsonPointer
import utils.parseAsJsonObject

fun main() {

    // This is an example of a very simple "JSON extraction DSL"
    val (firstName, town) = extractFrom(demoData) {
        (select string "firstName") to (select string "address" / "town")
    }

    println("$firstName from $town")

}

val demoData = """
        {
            "firstName": "Olaf",
            "lastName": "G.",
            "address": {
                "town": "Munich"
            }
        }
    """.parseAsJsonObject().getOrThrow()

// here we got our simple extraction DSL:
fun <T> extractFrom(data: Any?, extractor: ExtractionDsl.() -> T): T =
    ExtractionDsl(data).extractor()

class ExtractionDsl(private val data: Any?) {

    object SelectVerb

    val select = SelectVerb

    infix fun SelectVerb.string(field: String): String =
        select.string(JsonPointer.ROOT / field)

    infix fun SelectVerb.string(jsonPointer: JsonPointer): String =
        jsonPointer.extractFrom(data).getOrThrow().toString()

    operator fun String.div(other: String) = JsonPointer(this, other)
}
