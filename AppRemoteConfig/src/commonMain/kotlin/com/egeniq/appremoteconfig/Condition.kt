package com.egeniq.appremoteconfig

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

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
@Serializable
internal data class Condition(
    @Contextual val platform: Platform? = null,
    val platformVersion: VersionRange? = null,
    val appVersion: VersionRange? = null,
    val variant: String? = null,
    @Contextual val buildVariant: BuildVariant? = null,
    val language: String? = null
) {
    companion object {
        internal val MATCH_NEVER = Condition()
    }

    fun matches(
        platform: Platform,
        platformVersion: OperatingSystemVersion,
        appVersion: Version,
        variant: String? = null,
        buildVariant: BuildVariant,
        language: String?
    ): Boolean {
        if (this == MATCH_NEVER) {
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

internal object ConditionSerializer : KSerializer<Condition> {
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("appremoteconfig.Condition", Condition.serializer().descriptor)

    override fun deserialize(decoder: Decoder): Condition {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        val validKeys = Condition.serializer().descriptor.elementNames.toList()
        return if (jsonElement.jsonObject.keys.any { !validKeys.contains(it) }) {
            Condition.MATCH_NEVER
        } else {
            try {
                Json.decodeFromJsonElement<Condition>(jsonElement)
            } catch (ex: SerializationException) {
                Condition.MATCH_NEVER
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Condition) {
        error("Not implemented")
    }
}