package com.egeniq.appremoteconfig

import org.json.JSONObject

data class Override(
    val conditions: List<Condition>? = null,
    val schedule: Schedule? = null,
    val settings: JSONObject
) {
    constructor(json: JSONObject) : this(
        conditions = json.getJSONArray("matching")?.toList { Condition(it as JSONObject) },
//        schedule = json.getJSONObject("schedule")?.let { Schedule(it as JSONObject) },
        settings = json.getJSONObject("settings")
    )
}