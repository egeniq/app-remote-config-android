package com.egeniq.appremoteconfig

import org.json.JSONObject

data class Override(
    val conditions: List<Condition>? = null,
    val schedule: Schedule? = null,
    val settings: JSONObject
) {
    constructor(json: JSONObject) : this(
        conditions = json.getJSONArray("matching")?.toList { Condition(it as JSONObject) },
        schedule = if (json.has("schedule")) Schedule(json.getJSONObject("schedule")) else null,
        settings = json.getJSONObject("settings")
    )
}