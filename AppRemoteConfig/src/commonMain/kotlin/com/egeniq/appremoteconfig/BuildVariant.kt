package com.egeniq.appremoteconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Is the app compiled for debugging or for release?
 */
@Serializable
enum class BuildVariant {
    @SerialName("release")
    RELEASE,
    @SerialName("debug")
    DEBUG,
    @Transient
    UNKNOWN;
}