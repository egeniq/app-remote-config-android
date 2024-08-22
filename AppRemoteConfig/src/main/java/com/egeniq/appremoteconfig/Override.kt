package com.egeniq.appremoteconfig

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

data class Override(
    val conditions: List<Condition>? = null,
    val schedule: Schedule? = null,
    val settings: JsonObject,
) {
    constructor(json: JsonObject) : this(
        conditions = if (json.containsKey("matching")) {
            val conditionsListJson = json["matching"].toString()
            Json.decodeFromString<List<Condition>>(conditionsListJson)
        } else {
            emptyList()
        },
        schedule = if (json.containsKey("schedule")) {
            val scheduleJson = json["schedule"].toString()
            Json.decodeFromString<Schedule>(scheduleJson)
        } else {
            null
        },
        settings = json["settings"]?.jsonObject ?: JsonObject(emptyMap())
    )
}
