package com.egeniq.appremoteconfig

enum class Platform(val value: String) {
    IOS("iOS"),
    IOS_IPHONE("iOS.iPhone"),
    IOS_IPAD("iOS.iPad"),
    IOS_TV("iOS.TV"),
    IOS_CARPLAY("iOS.CarPlay"),
    IOS_MAC("iOS.Mac"),
    MACOS("macOS"),
    WATCHOS("watchOS"),
    VISIONOS("visionOS"),
    ANDROID("Android"),
    ANDROID_PHONE("Android.phone"),
    ANDROID_TABLET("Android.tablet"),
    ANDROID_TV("Android.TV"),
    WEAROS("WearOS"),
    UNKNOWN("unknown");

    internal fun appliesTo(other: Platform): Boolean {
        return when (this) {
            IOS -> other.value.startsWith(IOS.value)
            ANDROID -> other.value.startsWith(ANDROID.value)
            UNKNOWN -> false
            else -> this == other
        }
    }
}
