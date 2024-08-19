package com.egeniq.appremoteconfig

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

fun <T> JSONArray.toList(transform: (Any) -> T): List<T> {
    val list = mutableListOf<T>()
    for (i in 0 until length()) {
        list.add(transform(this[i]))
    }
    return list
}

fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until length()) {
        list.add(this[i])
    }
    return list
}

class Config(
    val settings: JSONObject,
    val deprecatedKeys: List<String> = emptyList(),
    val overrides: List<Override> = emptyList(),
    val meta: JSONObject
) {
    constructor(json: JSONObject) : this(
        settings = json.getJSONObject("settings"),
        deprecatedKeys = if (json.has("deprecatedKeys")) json.getJSONArray("deprecatedKeys")?.toList { it as String } ?: emptyList() else emptyList(),
        overrides = json.getJSONArray("overrides")?.toList { Override(it as JSONObject) } ?: emptyList(),
        meta = if (json.has("meta")) json.getJSONObject("meta") ?: JSONObject() else JSONObject()
    )


//    init {
//        json["settings"]?.let { jsonValue ->
//            if (jsonValue is Map<*, *>) {
//                @Suppress("UNCHECKED_CAST")
//                settings = jsonValue as Map<String, Any>
//            } else {
//                throw ConfigError.UnexpectedTypeForKey() // ConfigError("Unexpected type for key 'settings'")
//            }
//        }
//        deprecatedKeys = json["deprecatedKeys"] as? List<String> ?: emptyList()
//        overrides =
//            (json["overrides"] as? List<Map<String, Any>>)?.map { Override(it) } ?: emptyList()
//        meta = json["meta"] as? Map<String, Any> ?: emptyMap()
//    }

    fun resolve(
        date: Date,
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String? = null
    ): JSONObject {
        return overrides.fold(settings) { partialResult, override ->
            val isScheduled: Boolean = if (override.schedule != null) {
                override.schedule.contains(date)
            } else {
                true
            }

            val matches: Boolean = if (override.conditions != null) {
                override.conditions.any { condition ->
                    condition.matches(
                        platform,
                        platformVersion,
                        appVersion,
                        variant,
                        buildVariant,
                        language
                    )
                }
            } else {
                true
            }

            if (isScheduled && matches) {
                for (key in override.settings.keys()) {
                    partialResult.put(key, override.settings.get(key))
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
        language: String? = null
    ): List<Date> {
        var dates: List<Date> = emptyList()
        return overrides.fold(dates) { partialResult, override ->
            if (override.schedule != null) {
                val matches: Boolean = if (override.conditions != null) {
                    override.conditions.any { condition ->
                        condition.matches(
                            platform,
                            platformVersion,
                            appVersion,
                            variant,
                            buildVariant,
                            language
                        )
                    }
                } else {
                    true
                }

                if (matches) {
                    val from = override.schedule.from
                    val until = override.schedule.until
                    val dates = mutableListOf<Date>()
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
        }
            .sorted()
    }
}