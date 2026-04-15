package com.egeniq.appremoteconfig

import kotlinx.serialization.Serializable

/**
 * Is the app compiled for debugging or for release?
 */
@Serializable
enum class BuildVariant(val value: String) {
    RELEASE(value = "release"),
    DEBUG(value = "debug"),
    UNKNOWN(value = "unknown");
}