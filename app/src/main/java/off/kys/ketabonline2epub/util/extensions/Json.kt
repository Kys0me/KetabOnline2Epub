@file:Suppress("unused")

package off.kys.ketabonline2epub.util.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement

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