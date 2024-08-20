package com.egeniq.appremoteconfig

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


data class Override(
    val conditions: List<Condition>? = null,
    val schedule: Schedule? = null,
    val settings: JsonObject,
) {
    constructor(json: JsonObject) {
        Override.fromJsonObject(json)
    }

    companion object {
        fun fromJsonObject(json: JsonObject): Override {
            val conditions = if (json.contains("matching")) {
                val conditionsListJson = json["matching"].toString()
                Json.decodeFromString<List<Condition>>(conditionsListJson)
            } else {
                emptyList()
            }
            val schedule = if (json.containsKey("schedule")) {
                val scheduleJson = json["schedule"].toString()
                Json.decodeFromString<Schedule>(scheduleJson)
            } else {
                null
            }
            val settings: JsonObject = json["settings"]?.jsonObject ?: JsonObject(emptyMap())
            return Override(conditions, schedule, settings)
        }
    }
}
