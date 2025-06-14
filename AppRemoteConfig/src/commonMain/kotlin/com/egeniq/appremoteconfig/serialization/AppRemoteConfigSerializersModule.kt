package com.egeniq.appremoteconfig.serialization

import com.egeniq.appremoteconfig.BuildVariant
import com.egeniq.appremoteconfig.Condition
import com.egeniq.appremoteconfig.ConditionSerializer
import com.egeniq.appremoteconfig.Platform
import com.egeniq.appremoteconfig.Schedule
import com.egeniq.appremoteconfig.ScheduleSerializer
import kotlinx.serialization.modules.SerializersModule

val appRemoteConfigSerializersModule = SerializersModule {
    contextual(Condition::class, ConditionSerializer)
    contextual(Schedule::class, ScheduleSerializer)
    unknownEnumValue(Platform.UNKNOWN, Platform.serializer())
    unknownEnumValue(BuildVariant.UNKNOWN, BuildVariant.serializer())
}