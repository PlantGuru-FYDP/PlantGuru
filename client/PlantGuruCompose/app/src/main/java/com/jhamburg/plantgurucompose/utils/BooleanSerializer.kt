package com.jhamburg.plantgurucompose.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class BooleanSerializer : JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
    override fun serialize(
        src: Boolean?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(if (src == true) 1 else 0)
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Boolean {
        return json?.asInt == 1
    }
} 