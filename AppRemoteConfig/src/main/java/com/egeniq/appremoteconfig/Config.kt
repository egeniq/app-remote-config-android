package com.egeniq.appremoteconfig

import kotlinx.datetime.Instant
import org.json.JSONArray
import org.json.JSONObject
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import kotlin.jvm.Throws

internal fun <T> JSONArray.toList(transform: (Any) -> T): List<T> {
    val list = mutableListOf<T>()
    for (i in 0 until length()) {
        list.add(transform(this[i]))
    }
    return list
}

/**
 * A simple but effective way to manage apps remotely. A simple configuration file that is easy to maintain and host, yet provides important flexibility to specify settings based on your needs.
 *
 * @property settings The default settings that an app should use.
 * @property deprecatedKeys Keys that are no longer in use, but may still be used by overrides to accomodate older versions of an app.
 * @property overrides Overrides containing the settings to apply when they match and/or are scheduled. Applied from top to bottom.
 * @property meta Store metadata such as author or last updated date here.
 */
class Config(
    private val settings: JSONObject,
    private val deprecatedKeys: List<String> = emptyList(),
    private val overrides: List<Override> = emptyList(),
    private val meta: JSONObject
) {
    /**
     * Create a config from a JSON like structure
     *
     * @param json JSON describing the desired configuration according to this [scheme](https://raw.githubusercontent.com/egeniq/app-remote-config/main/Schema/appremoteconfig.schema.json)
     */
    constructor(json: JSONObject) : this(
        settings = json.getJSONObject("settings"),
        deprecatedKeys = if (json.has("deprecatedKeys")) json.getJSONArray("deprecatedKeys")?.toList { it as String } ?: emptyList() else emptyList(),
        overrides = if (json.has("overrides")) json.getJSONArray("overrides")?.toList { Override(it as JSONObject) } ?: emptyList() else emptyList(),
        meta = if (json.has("meta")) json.getJSONObject("meta") ?: JSONObject() else JSONObject()
    )

    companion object {
        /**
         * Create a config from JSON
         *
         * @param jsonString String containing JSON describing the desired configuration according to this [scheme](https://raw.githubusercontent.com/egeniq/app-remote-config/main/Schema/appremoteconfig.schema.json)
         */
        fun new(jsonString: String): Config {
            val json = JSONObject(jsonString)
            return Config(json)
        }

        private val sodiumClient = LazySodiumAndroid(SodiumAndroid())

        /**
         * Create a config from signed JSON
         *
         * @param signedJsonString String containing signed JSON describing the desired configuration according to this [scheme](https://raw.githubusercontent.com/egeniq/app-remote-config/main/Schema/appremoteconfig.schema.json)
         * @param publicKey Base64 encoded public key that was used to sign the data.
         */
        @Throws(ConfigError::class)
        fun new(signedJsonString: String, publicKey: String): Config {
            val verifiedJSONString = sodiumClient.cryptoSignOpen(signedJsonString, Key.fromBase64String(publicKey))
                ?: throw ConfigError.InvalidSignature()
            val json = JSONObject(verifiedJSONString)
            return Config(json)
        }
    }

    /**
     *Resolves which settings should be used by an app within its context
     *
     * @param date The date at which the settings are used
     * @param platform The platform on which the app runs
     * @param platformVersion The version of the platform on which the app runs
     * @param appVersion The version of the app that runs
     * @param variant The variant of the app that runs
     * @param buildVariant The build variant of the app that runs
     * @param language The language in which the app runs
     * @return A `JSONObject` containing the resolved settings.
     */
    fun resolve(
        date: Instant,
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

    /**
     * Lists all dates on which resolving the config could give other setings
     *
     * @param platform The platform on which the app runs
     * @param platformVersion The version of the platform on which the app runs
     * @param appVersion The version of the app that runs
     * @param variant The variant of the app that runs
     * @param buildVariant The build variant of the app that runs
     * @param language The language in which the app runs
     * @return A list of `Instant` representing the relevant dates.
     */
    public fun relevantResolutionDates(
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
        }
            .sorted()
    }
}