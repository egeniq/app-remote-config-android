package com.egeniq.appremoteconfig

/**
 * Range of versions
 */
sealed class VersionRange {
    /**
     * Matches an exact version
     *
     * Example string representation: \
     * `1.0.0`
     */
    data class Equal(val version: Version) : VersionRange()

    /**
     * Matches a version lesser than the given version, if the boolean is `true` the version is included
     *
     * Example string representations: \
     * `<1.0.0` '
     * `<=1.0.0`
     */
    data class LesserThan(val version: Version, val inclusive: Boolean) : VersionRange()

    /**
     * Matches a version greater than the given version, if the boolean is `true` the version is included
     *
     *  Example string representations: \
     *  `>1.0.0` \
     *  `>=1.0.0`
     */
    data class GreaterThan(val version: Version, val inclusive: Boolean) : VersionRange()

    /**
     * Matches a version between two given versions, if the boolean is `true` the version is included
     *
     *  Example string representations: \
     *  `1.0.0-2.0.0` (versions 1.0.0 and 2.0.0 are included) \
     *  `1.0.0>-2.0.0`(version 1.0.0 excluded and version 2.0.0 included) \
     *  `1.0.0-<2.0.0`(version 1.0.0 included and version 2.0.0 excluded) \
     *  `1.0.0>-<2.0.0` (versions 1.0.0 and 2.0.0 are excluded)
     */
    data class Between(val lower: Pair<Version, Boolean>, val upper: Pair<Version, Boolean>) :
        VersionRange()

    fun contains(other: Version): Boolean {
        return when (this) {
            is Equal -> other == version
            is LesserThan -> if (inclusive) other <= version else other < version
            is GreaterThan -> if (inclusive) other >= version else other > version
            is Between -> {
                when {
                    !lower.second && !upper.second -> other > lower.first && other < upper.first
                    !lower.second && upper.second -> other > lower.first && other <= upper.first
                    lower.second && !upper.second -> other >= lower.first && other < upper.first
                    else -> other >= lower.first && other <= upper.first
                }
            }
        }
    }

    val rawValue: String
        get() {
            return when (this) {
                is Equal -> version.rawValue
                is LesserThan -> if (inclusive) "<=${version.rawValue}" else "<${version.rawValue}"
                is GreaterThan -> if (inclusive) ">=${version.rawValue}" else ">${version.rawValue}"
                is Between -> {
                    when {
                        !lower.second && !upper.second -> "${lower.first.rawValue}>-<${upper.first.rawValue}"
                        !lower.second && upper.second -> "${lower.first.rawValue}>-${upper.first.rawValue}"
                        lower.second && !upper.second -> "${lower.first.rawValue}-<${upper.first.rawValue}"
                        else -> "${lower.first.rawValue}-${upper.first.rawValue}"
                    }
                }
            }
        }

    companion object {
        @Throws(ConfigError::class)
        fun fromRawValue(rawValue: String): VersionRange {
            val parts = rawValue.split("-")
            when (parts.size) {
                2 -> {
                    val lower = parts[0]
                    val lowerIncluded = !lower.endsWith(">")
                    val lowerVersion = Version(lower.dropLast(if (lowerIncluded) 0 else 1))
                    val upper = parts[1]
                    val upperIncluded = !upper.startsWith("<")
                    val upperVersion = Version(upper.drop(if (upperIncluded) 0 else 1))
                    return Between(
                        lower = Pair(lowerVersion, lowerIncluded),
                        upper = Pair(upperVersion, upperIncluded)
                    )
                }

                1 -> {
                    val part = parts[0]
                    return when {
                        part.startsWith("<=") || part.startsWith("=<") -> {
                            val version = Version(part.drop(2))
                            LesserThan(version, true)
                        }

                        part.startsWith("<") -> {
                            val version = Version(part.drop(1))
                            LesserThan(version, false)
                        }

                        part.startsWith(">=") || part.startsWith("=>") -> {
                            val version = Version(part.drop(2))
                            GreaterThan(version, true)
                        }

                        part.startsWith(">") -> {
                            val version = Version(part.drop(1))
                            GreaterThan(version, false)
                        }

                        part.startsWith("=") -> {
                            val version = Version(part.drop(1))
                            Equal(version)
                        }

                        else -> {
                            val version = Version(part)
                            Equal(version)
                        }
                    }
                }

                else -> {
                    throw ConfigError.InvalidVersionRange()
                }
            }
        }
    }
}
