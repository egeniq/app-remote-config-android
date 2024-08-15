package com.egeniq.appremoteconfig

enum class Platform(val value: String) {
    iOS("iOS"),
    iOS_iPhone("iOS.iPhone"),
    iOS_iPad("iOS.iPad"),
    iOS_tv("iOS.TV"),
    iOS_carplay("iOS.CarPlay"),
    iOS_mac("iOS.Mac"),
    macOS("macOS"),
    watchOS("watchOS"),
    visionOS("visionOS"),
    android("Android"),
    android_phone("Android.phone"),
    android_tablet("Android.tablet"),
    android_tv("Android.TV"),
    wearOS("WearOS"),
    unknown("");

    fun appliesTo(other: Platform): Boolean {
        return when (this) {
            iOS -> other.value.startsWith(iOS.value)
            android -> other.value.startsWith(android.value)
            unknown -> false
            else -> this == other
        }
    }
}
