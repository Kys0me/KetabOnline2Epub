@file:Suppress("unused")

package off.kys.ketabonline2epub.util.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken

/**
 * Safely extracts a String from a [JsonElement].
 *
 * This avoids crashes by verifying that the element is not null,
 * is not a JSON null literal, and is a valid JSON primitive string.
 *
 * @return The string value if valid; null otherwise.
 */
fun JsonElement?.safeString(): String? =
    when {
        this == null -> null
        this.isJsonNull -> null
        this.isJsonPrimitive && this.asJsonPrimitive.isString -> this.asString
        else -> null
    }

/**
 * Safely extracts an Int from a [JsonElement].
 *
 * This avoids crashes by verifying that the element is not null,
 * is not a JSON null literal, and is a valid JSON primitive integer.
 *
 * @return The integer value if valid; null otherwise.
 */
fun JsonElement?.safeInt(): Int? =
    when {
        this == null -> null
        this.isJsonNull -> null
        this.isJsonPrimitive && this.asJsonPrimitive.isNumber -> this.asInt
        else -> null
    }

/**
 * Safely casts a [JsonElement] to a [JsonArray].
 * * Useful when parsing API responses where a field might be an array
 * or missing/null depending on the result.
 *
 * @return The [JsonArray] if the element is an array; null otherwise.
 */
fun JsonElement?.safeArray(): JsonArray? =
    when {
        this == null -> null
        this.isJsonArray -> this.asJsonArray
        else -> null
    }

/**
 * Reads the next token from the JSON stream and asserts that it is a string value.
 *
 * This method is similar to `nextString()` but allows the value to be `null`.
 * It consumes the next token and returns its string value. If the token is a JSON null,
 * it consumes the null and returns a `null` string.
 *
 * @return The string value of the next token, or `null` if the token is a JSON null.
 * @throws java.io.IOException if there is an I/O error during parsing.
 * @throws com.google.gson.stream.MalformedJsonException if the next token is not a string or JSON null.
 */
fun JsonReader.nextNullableString(): String? = if (peek() == JsonToken.NULL) {
    nextNull()
    null
} else {
    nextString()
}