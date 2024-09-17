package com.egeniq.appremoteconfig

/**
 * Lists supported platforms
 */
enum class Platform(val value: String) {
    /** iOS in any of its variants */
    IOS("iOS"),
    /** iOS on an iPhone */
    IOS_IPHONE("iOS.iPhone"),
    /** iOS on an iPad */
    IOS_IPAD("iOS.iPad"),
    /** iOS on an Apple TV */
    IOS_TV("iOS.TV"),
    /** iOS on CarPlay */
    IOS_CARPLAY("iOS.CarPlay"),
    /** iOS on an a Mac using Catalyst */
    IOS_MAC("iOS.Mac"),
    /** Linux */
    LINUX("Linux"),
    /** macOS */
    MACOS("macOS"),
    /** watchOS */
    WATCHOS("watchOS"),
    /** visionOS */
    VISIONOS("visionOS"),
    /** Android in any of its variants */
    ANDROID("Android"),
    /** Android on a phone */
    ANDROID_PHONE("Android.phone"),
    /** Android on a tablet */
    ANDROID_TABLET("Android.tablet"),
    /** Android on a tv */
    ANDROID_TV("Android.TV"),
    /** WearOS */
    WEAROS("WearOS"),
    /** Windows */
    WINDOWS("Windows"),
    /** Unknown */
    UNKNOWN("unknown");

    internal fun appliesTo(other: Platform): Boolean {
        // self can be ios
        // other can be ios.iphone
        // then match

        // self can be ios.iphone
        // other can be ios
        // then DO NOT match
        return when (this) {
            IOS -> other.value.startsWith(IOS.value)
            ANDROID -> other.value.startsWith(ANDROID.value)
            UNKNOWN -> false
            else -> this == other
        }
    }
}
