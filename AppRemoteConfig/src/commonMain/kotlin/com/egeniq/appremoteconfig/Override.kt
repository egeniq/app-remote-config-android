package com.egeniq.appremoteconfig

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Override containing the settings to apply when it matches and/or is scheduled.
 *
 * When an app matches with one of the conditions and if a schedule is set it contains the current time, the settings will override the default settings. The settings must use keys that are in use in the default settings or are listed as deprecated keys.
 *
 * @param conditions To be considered an override should match at least one of the conditions.
 * @param schedule Schedule to limit overriding settings in time
 * @param settings The additional settings that are applied when the override is applied. The keys should be either in use or listed as deprecated.
 */
@Serializable
internal data class Override(
    @SerialName("matching")
    val conditions: List<@Contextual Condition>? = null,
    val schedule: Schedule? = null,
    val settings: JsonElement
)