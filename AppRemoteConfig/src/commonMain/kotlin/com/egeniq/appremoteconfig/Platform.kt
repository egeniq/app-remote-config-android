package com.egeniq.appremoteconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Lists supported platforms
 */
@Serializable
enum class Platform {
    /** iOS in any of its variants */
    @SerialName("iOS")
    IOS,

    /** iOS on an iPhone */
    @SerialName("iOS.iPhone")
    IOS_IPHONE,

    /** iOS on an iPad */
    @SerialName("iOS.iPad")
    IOS_IPAD,

    /** iOS on an Apple TV */
    @SerialName("iOS.TV")
    IOS_TV,

    /** iOS on CarPlay */
    @SerialName("iOS.CarPlay")
    IOS_CARPLAY,

    /** iOS on an a Mac using Catalyst */
    @SerialName("iOS.Mac")
    IOS_MAC,

    /** Linux */
    @SerialName("Linux")
    LINUX,

    /** macOS */
    @SerialName("macOS")
    MACOS,

    /** watchOS */
    @SerialName("watchOS")
    WATCHOS,

    /** visionOS */
    @SerialName("visionOS")
    VISIONOS,

    /** Android in any of its variants */
    @SerialName("Android")
    ANDROID,

    /** Android on a phone */
    @SerialName("Android.phone")
    ANDROID_PHONE,

    /** Android on a tablet */
    @SerialName("Android.tablet")
    ANDROID_TABLET,

    /** Android on a tv */
    @SerialName("Android.TV")
    ANDROID_TV,

    /** WearOS */
    @SerialName("WearOS")
    WEAROS,

    /** Windows */
    @SerialName("Windows")
    WINDOWS,

    @Transient
    /** an unknown / incorrect or future value **/
    UNKNOWN;

    internal fun appliesTo(other: Platform): Boolean {
        // self can be ios
        // other can be ios.iphone
        // then match

        // self can be ios.iphone
        // other can be ios
        // then DO NOT match
        return when (this) {
            IOS -> other in listOf(IOS_IPHONE, IOS_IPAD, IOS_TV, IOS_CARPLAY, MACOS)
            ANDROID -> other in listOf(ANDROID_PHONE, ANDROID_TABLET, ANDROID_TV)
            UNKNOWN -> false
            else -> this == other
        }
    }
}

