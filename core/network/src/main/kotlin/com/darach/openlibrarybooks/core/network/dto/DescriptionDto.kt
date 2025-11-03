package com.darach.openlibrarybooks.core.network.dto

import androidx.annotation.Keep
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * Sealed class representing a work's description.
 *
 * The Open Library API returns descriptions polymorphically:
 * - As a plain string: "This is a description"
 * - As an object with value: {"value": "This is a description"}
 *
 * This sealed class handles both cases elegantly.
 */
@Keep
sealed class DescriptionDto {
    /**
     * Description as plain text string.
     */
    data class Text(val value: String) : DescriptionDto()

    /**
     * Description as an object containing a value field.
     */
    data class ObjectValue(val value: String) : DescriptionDto()

    /**
     * Gets the actual description text regardless of the underlying format.
     */
    fun getDescription(): String = when (this) {
        is Text -> value
        is ObjectValue -> value
    }
}

/**
 * Custom Gson deserializer for handling polymorphic description field.
 *
 * Automatically detects whether the JSON contains a string or object
 * and deserializes to the appropriate DescriptionDto subtype.
 */
class DescriptionDeserializer : JsonDeserializer<DescriptionDto> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DescriptionDto =
        when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                DescriptionDto.Text(json.asString)
            }
            json.isJsonObject -> {
                val value = json.asJsonObject.get("value")?.asString
                    ?: throw JsonParseException("Description object missing 'value' field")
                DescriptionDto.ObjectValue(value)
            }
            else -> throw JsonParseException("Unexpected description format: $json")
        }
}
