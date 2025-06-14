package com.egeniq.appremoteconfig.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModuleBuilder

internal class UnknownValueSerializer<T : Enum<T>>(
    private val unknownValue: T,
    private val serializer: KSerializer<T>
) :
    KSerializer<T?> {
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("UnknownSerializer", serializer.descriptor)

    override fun deserialize(decoder: Decoder): T? {
        try {
            val value = decoder.decodeSerializableValue(serializer)
            return value
        } catch (ex: SerializationException) {
            return unknownValue
        }
    }

    override fun serialize(encoder: Encoder, value: T?) {
        error("Not implemented")
    }
}

internal inline fun <reified T : Enum<T>> SerializersModuleBuilder.unknownEnumValue(
    value: T,
    serializer: KSerializer<T>
) {
    contextual(value::class) {
        UnknownValueSerializer(value, serializer)
    }
}
