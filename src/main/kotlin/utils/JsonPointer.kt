/**
 * Olaf Gottschalk, SoCraTes 2022
 */

package utils

/**
 * The utils.JsonPointer class implements much of RFC6901 Json Pointers for plain Map<Any?, Any?> structures.
 *
 * It supports the following data structures to crawl:
 * - Map<Any?, Any?> for JSON objects
 * - List<Any?>, Array<Any?> and primitive arrays of chars, bytes, shorts, ints, longs and booleans for JSON arrays
 *
 * @constructor accepts a list of reference tokens representing each level to traverse
 * @property size denotes the number of tokens (= depth) with an empty pointer (to ROOT) having size 0
 * @property isRoot true if this instance is an empty pointer with zero reference tokens
 */
@Suppress("MemberVisibilityCanBePrivate")
data class JsonPointer(val tokens: List<String>) {
    constructor(vararg tokens: String) : this(listOf(*tokens))

    val size: Int = tokens.size
    val isRoot: Boolean = size == 0

    /**
     * Extracts the referenced element from a given data object.
     * @param data the data object to extract from
     * @return a [Result] indicating success in extraction of an element or failure if this JSON pointer's reference
     * is not found in the data
     */
    fun extractFrom(data: Any?): Result<Any?> =
        data.extract(0)

    companion object {
        const val DELIMITER = "/"
        val ROOT = JsonPointer(emptyList())

        private fun escape(s: String) =
            s.replace("~", "~0").replace("/", "~1")

        private class NotFound(pointer: JsonPointer) : IllegalArgumentException("no element found at '$pointer'")

    }

    override fun toString(): String = stringRepresentation

    operator fun div(token: String): JsonPointer =
        JsonPointer(tokens + token)

    operator fun div(token: Int): JsonPointer =
        JsonPointer(tokens + "$token")

    operator fun plus(other: JsonPointer): JsonPointer =
        JsonPointer(this.tokens + other.tokens)

    operator fun contains(other: JsonPointer): Boolean =
        other.size <= this.size && other.tokens.indices.all { tokens[it] == other.tokens[it] }

    private val stringRepresentation: String by lazy {
        tokens.joinToString("") { "$DELIMITER${escape(it)}" }
    }

    private tailrec fun Any?.extract(level: Int): Result<Any?> {
        if (this is NotFound) return Result.failure(this)
        if (level == tokens.size) return Result.success(this)

        val token = tokens[level]
        val element = when {
            this is Map<*, *> -> getOrDefault(token, notFound())
            this is List<*> -> token.toIntOrNull().let { index -> if (index in indices) this[index!!] else notFound() }
            this is Array<*> -> token.toIntOrNull().let { index -> if (index in indices) this[index!!] else notFound() }
            this != null && this::class.java.isArray ->
                token.toIntOrNull()?.let { index ->
                    when (this) {
                        is CharArray -> getOrNull(index)
                        is ShortArray -> getOrNull(index)
                        is IntArray -> getOrNull(index)
                        is ByteArray -> getOrNull(index)
                        is LongArray -> getOrNull(index)
                        is FloatArray -> getOrNull(index)
                        is DoubleArray -> getOrNull(index)
                        is BooleanArray -> getOrNull(index)
                        else -> null
                    }
                } ?: notFound()

            else -> notFound()
        }
        return element.extract(level + 1)
    }

    private fun notFound() = NotFound(this@JsonPointer)

}
