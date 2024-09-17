package com.egeniq.appremoteconfig

import org.json.JSONObject

/**
 * Override containing the settings to apply when it matches and/or is scheduled.
 *
 * When an app matches with one of the conditions and if a schedule is set it contains the current time, the settings will override the default settings. The settings must use keys that are in use in the default settings or are listed as deprecated keys.
 *
 * @param conditions To be considered an override should match at least one of the conditions.
 * @param scheduleSchedule to limit overriding settings in time
 * @param settings The additional settings that are applied when the override is applied. The keys should be either in use or listed as deprecated.
 */
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