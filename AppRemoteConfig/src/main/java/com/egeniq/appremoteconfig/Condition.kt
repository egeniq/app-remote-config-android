package com.egeniq.appremoteconfig

import org.json.JSONObject

enum class BuildVariant(val value: String) {
    RELEASE(value = "release"),
    DEBUG(value = "debug"),
    UNKNOWN(value = "unknown");
}

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
        matchNever = json.keys().asSequence().any { key ->
            !listOf(
                "platform",
                "platformVersion",
                "appVersion",
                "variant",
                "buildVariant",
                "language"
            ).contains(key)
        },
        platform = if (json.has("platform")) json.getString("platform")
            .let { platform -> Platform.entries.firstOrNull { it.value == platform } }
            ?: Platform.unknown else null, // TODO Set matchNever true instead of crash for unknown strings
        platformVersion = if (json.has("platformVersion")) json.getString("platformVersion")
            .let { VersionRange.fromRawValue(it) } else null, // TODO Set matchNever true instead of crash for unknown strings
        appVersion = if (json.has("appVersion")) json.getString("appVersion")
            .let { VersionRange.fromRawValue(it) } else null, // TODO Set matchNever true instead of crash for unknown strings
        variant = if (json.has("variant")) json.getString("variant") else null,
        buildVariant = if (json.has("buildVariant")) json.getString("buildVariant")
            .let { buildVariant ->
                BuildVariant.entries.firstOrNull() {
                    it.value == buildVariant
                }
            } ?: BuildVariant.UNKNOWN else null,// TODO Set matchNever true instead of crash for unknown strings
        language = if (json.has("language")) json.getString("language") else null
    )

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
