package com.egeniq.appremoteconfig

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class Config(
    val settings: JsonObject,
    val deprecatedKeys: List<String> = emptyList(),
    val overrides: List<Override> = emptyList(),
    val meta: JsonObject,
) {
    constructor(json: JsonObject) : this(
        settings = json["settings"]?.jsonObject ?: buildJsonObject { },
        deprecatedKeys = if (json.containsKey("deprecatedKeys")) json["deprecatedKeys"]?.jsonArray
            ?.map { it.jsonPrimitive.content } ?: emptyList() else emptyList(),
        overrides = json["overrides"]?.jsonArray?.map { Override(it.jsonObject) } ?: emptyList(),
        meta = if (json.containsKey("meta")) json["meta"]?.jsonObject ?: buildJsonObject { } else buildJsonObject { }
    )

    fun resolve(
        date: Instant,
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String? = null,
    ): JsonObject {
        return overrides.fold(settings) { partialResult, override ->
            val isScheduled: Boolean = if (override.schedule != null) {
                override.schedule.contains(date)
            } else {
                true
            }

            val matches: Boolean = if (override.conditions != null) {
                override.conditions.any { condition ->
                    condition.matches(
                        platform, platformVersion, appVersion, variant, buildVariant, language
                    )
                }
            } else {
                true
            }

            if (isScheduled && matches) {
                for (key in override.settings.keys) {
//  TODO:                 partialResult.plus(key, override.settings[key])
                }
                partialResult
            } else {
                partialResult
            }
        }
    }

    fun relevantResolutionDates(
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String? = null,
    ): List<Instant> {
        var dates: List<Instant> = emptyList()
        return overrides.fold(dates) { partialResult, override ->
            if (override.schedule != null) {
                val matches: Boolean = if (override.conditions != null) {
                    override.conditions.any { condition ->
                        condition.matches(
                            platform, platformVersion, appVersion, variant, buildVariant, language
                        )
                    }
                } else {
                    true
                }

                if (matches) {
                    val from = override.schedule.from
                    val until = override.schedule.until
                    val dates = mutableListOf<Instant>()
                    if (from != null) {
                        dates.add(from)
                    }
                    if (until != null) {
                        dates.add(until)
                    }
                    partialResult.plus(dates)
                } else {
                    partialResult
                }
            } else {
                partialResult
            }
        }.sorted()
    }
}