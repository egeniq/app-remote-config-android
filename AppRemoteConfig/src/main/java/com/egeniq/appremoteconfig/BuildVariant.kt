package com.egeniq.appremoteconfig

/**
 * Is the app compiled for debugging or for release?
 */
enum class BuildVariant(val value: String) {
    RELEASE(value = "release"),
    DEBUG(value = "debug"),
    UNKNOWN(value = "unknown");
}