package com.egeniq.appremoteconfig

import kotlinx.serialization.Serializable

/**
 * Version following semantic versioning convention
 */
@Serializable
data class Version(val canonical: Triple<Int, Int, Int>) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        return when {
            canonical.first == other.canonical.first && canonical.second == other.canonical.second -> canonical.third.compareTo(
                other.canonical.third
            )

            canonical.first == other.canonical.first -> canonical.second.compareTo(other.canonical.second)
            else -> canonical.first.compareTo(other.canonical.first)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Version) return false
        return canonical == other.canonical
    }

    override fun hashCode(): Int {
        return canonical.hashCode()
    }

    constructor(rawValue: String) : this(parseVersion(rawValue))

    constructor(version: OperatingSystemVersion) : this(
        Triple(
            version.majorVersion,
            version.minorVersion,
            version.patchVersion
        )
    )

    val operatingSystemVersion: OperatingSystemVersion
        get() = OperatingSystemVersion(canonical.first, canonical.second, canonical.third)

    companion object {
        private fun parseVersion(rawValue: String): Triple<Int, Int, Int> {
            val trimmedValue = rawValue.trimStart { !"1234567890.".contains(it) }
            val parts = trimmedValue.split(".").mapNotNull { it.toIntOrNull() }.take(3)
            require(parts.isNotEmpty()) { "Invalid version format" }
            val padded = parts + List(3 - parts.size) { 0 }
            return Triple(padded[0], padded[1], padded[2])
        }
    }
}

@Serializable
data class OperatingSystemVersion(
    val majorVersion: Int,
    val minorVersion: Int,
    val patchVersion: Int
)