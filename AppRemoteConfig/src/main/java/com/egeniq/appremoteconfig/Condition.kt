package com.egeniq.appremoteconfig

import org.json.JSONObject

/**
 * To be considered a match, the condition should match all properties.
 *
 * @param platform The platform the app is running on.
 * @param platformVersion The semantic version of platform the app is running on.
 * @param appVersion The semantic version of the app.
 * @param variant The variant of the app.
 * @param buildVariant The build variant of the app.
 * @param language The language the app is using currently as two character code.
 */
data class Condition(
    val matchNever: Boolean,
    val platform: Platform?,
    val platformVersion: VersionRange?,
    val appVersion: VersionRange?,
    val variant: String?,
    val buildVariant: BuildVariant?,
    val language: String?
) {
    constructor(json: JSONObject) : this(
        // If there is a new unknown key, never match condition
        matchNever = json.keys().asSequence().any { key ->
            !listOf(
                "platform",
                "platformVersion",
                "appVersion",
                "variant",
                "buildVariant",
                "language"
            ).contains(key)
        } || parseFields(json),
        platform = parsePlatform(json),
        platformVersion = parsePlatformVersion(json),
        appVersion = parseAppVersion(json),
        variant = parseVariant(json),
        buildVariant = parseBuildVariant(json),
        language = parseLanguage(json)
    )

    companion object {
        private fun parseFields(json: JSONObject): Boolean {
            var matchNever = false
            if (json.has("platform")) {
                val platform = parsePlatform(json)
                if (platform == null) {
                    matchNever = true
                }
            }

            if (json.has("platformVersion")) {
                val platformVersion = parsePlatformVersion(json)
                if (platformVersion == null) {
                    matchNever = true
                }
            }

            if (json.has("appVersion")) {
                val appVersion = parseAppVersion(json)
                if (appVersion == null) {
                    matchNever = true
                }
            }

            if (json.has("variant")) {
                val variant = parseVariant(json)
                if (variant == null) {
                    matchNever = true
                }
            }

            if (json.has("buildVariant")) {
                val buildVariant = parseBuildVariant(json)
                if (buildVariant == null) {
                    matchNever = true
                }
            }

            if (json.has("language")) {
                val language = parseLanguage(json)
                if (language == null) {
                    matchNever = true
                }
            }

            return matchNever
        }

        private fun parsePlatform(json: JSONObject): Platform? {
            return json.optString("platform").let { platform ->
                try {
                    Platform.entries.firstOrNull { it.value == platform }
                } catch (e: Exception) {
                    null
                }
            }
        }

        private fun parsePlatformVersion(json: JSONObject): VersionRange? {
            return json.optString("platformVersion").let {
                try {
                    VersionRange.fromRawValue(it)
                } catch (e: Exception) {
                    null
                }
            }
        }

        private fun parseAppVersion(json: JSONObject): VersionRange? {
            return json.optString("appVersion").let {
                try {
                    VersionRange.fromRawValue(it)
                } catch (e: Exception) {
                    null
                }
            }
        }

        private fun parseVariant(json: JSONObject): String? {
            return if (json.has("variant")) json.getString("variant") else null
        }

        private fun parseBuildVariant(json: JSONObject): BuildVariant? {
            return json.optString("buildVariant").let { buildVariant ->
                try {
                    BuildVariant.entries.firstOrNull { it.value == buildVariant }
                } catch (e: Exception) {
                    null
                }
            }
        }

        private fun parseLanguage(json: JSONObject): String? {
            return if (json.has("language")) json.getString("language") else null
        }
    }

    fun matches(
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String?
    ): Boolean {
        if (matchNever) {
            return false
        }

        if (this.platform != null && !this.platform.appliesTo(platform)) {
            return false
        }

        if (this.platformVersion != null && !this.platformVersion.contains(Version(platformVersion))) {
            return false
        }

        if (this.appVersion != null && !this.appVersion.contains(appVersion)) {
            return false
        }

        if (variant != null && this.variant != null && !this.variant.contains(variant)) {
            return false
        }

        if (this.buildVariant != null && this.buildVariant != buildVariant) {
            return false
        }

        if (language != null && this.language != null && !language.startsWith(this.language)) {
            return false
        }

        return true
    }
}