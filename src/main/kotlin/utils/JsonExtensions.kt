package utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * Tries to parse a String to a JSON object, represented by a Map<String, Any?>.
 * @return a [Result] wrapping the parsed data
 */
fun String.parseAsJsonObject(): Result<Map<String, Any?>> = runCatching {
    objectMapper.readValue(this)
}

private val objectMapper: ObjectMapper = jacksonObjectMapper()
    .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
    .enable(JsonParser.Feature.ALLOW_COMMENTS)
    .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
