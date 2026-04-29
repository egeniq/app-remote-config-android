package com.egeniq.appremoteconfig

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * A schedule describes a period of time.
 *
 * Omitting `from` means distant past and omitting `until` means distant future. Omitting both means the schedule will never be matched.
 *
 * @param from `Instant` from which onwards the settings should be applied
 * @param until `Instant` from which onwards the settings should not be applied anymore.
 */
@Serializable
data class Schedule(
    var from: Instant? = null,
    var until: Instant? = null
) {
    fun contains(date: Instant): Boolean {
        val from = from
        val until = until

        if (from == null && until == null) {
            return false
        }
        if (from != null && date < from) {
            return false
        }
        if (until != null && date >= until) {
            return false
        }
        return true
    }
}

internal object ScheduleSerializer : KSerializer<Schedule> {
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("appremoteconfig.Schedule", Schedule.serializer().descriptor)

    override fun deserialize(decoder: Decoder): Schedule {
        return try {
            decoder.decodeSerializableValue(Schedule.serializer())
        } catch (ex: SerializationException) {
            Schedule(null, null)
        }
    }

    override fun serialize(encoder: Encoder, value: Schedule) {
        error("Not implemented")
    }
}
