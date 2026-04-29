package com.egeniq.appremoteconfig

import com.egeniq.appremoteconfig.serialization.appRemoteConfigSerializersModule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.time.Instant

private val appRemoteConfigJson = Json {
    ignoreUnknownKeys = true
    serializersModule = appRemoteConfigSerializersModule
}

/**
 * Creates a config from a json string
 * @param configJson the config
 * @return the config instance
 */
fun Config(configJson: String): Config {
    return appRemoteConfigJson.decodeFromString(configJson)
}

/**
 * Resolves which settings should be used by an app within its context
 *
 * @param date The date at which the settings are used
 * @param platform The platform on which the app runs
 * @param platformVersion The version of the platform on which the app runs
 * @param appVersion The version of the app that runs
 * @param variant The variant of the app that runs
 * @param buildVariant The build variant of the app that runs
 * @param language The language in which the app runs
 * @return A [JsonObject] containing the resolved settings.
 */
fun Config.resolve(
    date: Instant,
    platform: Platform,
    platformVersion: OperatingSystemVersion,
    appVersion: Version,
    variant: String? = null,
    buildVariant: BuildVariant,
    language: String? = null
): JsonObject {
    return resolve(
        JsonObject.serializer(),
        date,
        platform,
        platformVersion,
        appVersion,
        variant,
        buildVariant,
        language
    )
}

/**
 * Resolves which settings should be used by an app within its context into a typed object.
 * Note: the deserializer should handle unknown and missing keys.
 *
 * When using the default generated serializers,settings object should be annotated
 * with [JsonIgnoreUnknownKeys] and have defaults set for its properties,
 * otherwise deserialization might fail at runtime.
 *
 * @param deserializer The deserializer to use.
 * @param date The date at which the settings are used
 * @param platform The platform on which the app runs
 * @param platformVersion The version of the platform on which the app runs
 * @param appVersion The version of the app that runs
 * @param variant The variant of the app that runs
 * @param buildVariant The build variant of the app that runs
 * @param language The language in which the app runs
 * @return The deserialized settings
 */
fun <T> Config.resolve(
    deserializer: KSerializer<T>,
    date: Instant,
    platform: Platform,
    platformVersion: OperatingSystemVersion,
    appVersion: Version,
    variant: String? = null,
    buildVariant: BuildVariant,
    language: String? = null
): T {
    return Json.decodeFromJsonElement(
        deserializer,
        resolveInternal(
            date,
            platform,
            platformVersion,
            appVersion,
            variant,
            buildVariant,
            language
        )
    )
}

/**
 * Return the meta data for the config as a typed object
 * @param deserializer the deserializer to use
 * @return T the meta data or null if not present in the config
 */
fun <T> Config.meta(deserializer: KSerializer<T>): T? {
    return meta?.let { Json.decodeFromJsonElement(deserializer, it) }
}

/**
 * A simple but effective way to manage apps remotely. A simple configuration file that is easy to maintain and host, yet provides important flexibility to specify settings based on your needs.
 *
 * @property settings The default settings that an app should use.
 * @property deprecatedKeys Keys that are no longer in use, but may still be used by overrides to accommodate older versions of an app.
 * @property overrides Overrides containing the settings to apply when they match and/or are scheduled. Applied from top to bottom.
 * @property meta Store metadata such as author or last updated date here.
 */
@Serializable
class Config internal constructor(
    private val settings: JsonElement,
    private val deprecatedKeys: List<String> = emptyList(),
    private val overrides: List<Override> = emptyList(),
    internal val meta: JsonElement? = null,
) {
    /**
     * Resolves which settings should be used by an app within its context
     *
     * @param date The date at which the settings are used
     * @param platform The platform on which the app runs
     * @param platformVersion The version of the platform on which the app runs
     * @param appVersion The version of the app that runs
     * @param variant The variant of the app that runs
     * @param buildVariant The build variant of the app that runs
     * @param language The language in which the app runs
     * @return A [JsonElement] containing the resolved settings.
     */
    internal fun resolveInternal(
        date: Instant,
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String? = null
    ): JsonElement {
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
                val partialObject = partialResult.jsonObject
                buildJsonObject {
                    for (entry in partialObject) {
                        put(entry.key, entry.value)
                    }
                    for (entry in override.settings.jsonObject.entries) {
                        put(entry.key, entry.value)
                    }
                }
            } else {
                partialResult
            }
        }
    }

    /**
     * Lists all dates on which resolving the config could give other settings
     *
     * @param platform The platform on which the app runs
     * @param platformVersion The version of the platform on which the app runs
     * @param appVersion The version of the app that runs
     * @param variant The variant of the app that runs
     * @param buildVariant The build variant of the app that runs
     * @param language The language in which the app runs
     * @return A list of `Instant` representing the relevant dates.
     */
    fun relevantResolutionDates(
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String? = null
    ): List<Instant> {
        val dates: List<Instant> = emptyList()
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
                    partialResult + listOfNotNull(
                        override.schedule.from,
                        override.schedule.until
                    )
                } else {
                    partialResult
                }
            } else {
                partialResult
            }
        }.sorted()
    }
}