package com.egeniq.appremoteconfig

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

enum class BuildVariant {
    RELEASE,
    DEBUG,
    UNKNOWN
}

@Serializable
data class Condition(
    val matchNever: Boolean = true,
    val platform: Platform? = null,
    val platformVersion: VersionRange? = null,
    val appVersion: VersionRange? = null,
    val variant: String? = null,
    val buildVariant: BuildVariant? = null,
    val language: String? = null,
) {
    constructor(json: JsonObject) : this(
        matchNever = json.keys.any { key ->
            !listOf(
                "platform",
                "platformVersion",
                "appVersion",
                "variant",
                "buildVariant",
                "language"
            ).contains(key)
        },
        platform = json["platform"]?.jsonPrimitive?.contentOrNull?.let { platform ->
            Platform.entries.firstOrNull { it.value == platform } ?: Platform.unknown
        },
        platformVersion = json["platformVersion"]?.jsonPrimitive?.contentOrNull?.let {
            VersionRange.fromRawValue(it.toString())
        },
        appVersion = json["appVersion"]?.jsonPrimitive?.contentOrNull?.let {
            VersionRange.fromRawValue(it.toString())
        },
        variant = json["variant"]?.jsonPrimitive?.contentOrNull,
        buildVariant = json["buildVariant"]?.jsonPrimitive?.contentOrNull?.let {
            BuildVariant.valueOf(it.toString())
        },
        language = json["language"]?.jsonPrimitive?.contentOrNull
    )

    fun matches(
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String?,
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
