package off.kys.ketabonline2epub.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement

fun JsonElement?.safeString(): String? =
    when {
        this == null -> null
        this.isJsonNull -> null
        this.isJsonPrimitive && this.asJsonPrimitive.isString -> this.asString
        else -> null
    }

fun JsonElement?.safeArray(): JsonArray? =
    when {
        this == null -> null
        this.isJsonArray -> this.asJsonArray
        else -> null
    }
