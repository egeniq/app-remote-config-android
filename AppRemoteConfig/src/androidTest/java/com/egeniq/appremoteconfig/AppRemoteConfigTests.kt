package com.egeniq.appremoteconfig

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AppRemoteConfigTests {

    companion object {
        val dateFormatter =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // Quoted "Z" to indicate UTC, no timezone offset is weird!
    }

    @Test
    fun parsing() {
        val jsonString = """
        {
            "settings": {
                "foo": true,
                "bar": "hello world",
                "baz": [
                    {
                        "abc": "def"
                    }
                ],
                "updateRequired": false,
                "updateRecommended": false,
                "appDisabled": false
            },
            "deprecatedKeys": [
                "old1",
                "old3"
            ],
            "overrides": [
                {
                    "matching": [
                        {
                            "variant": "AppStore"
                        }
                    ],
                    "settings": {
                        "foo": false
                    }
                },
                {
                    "matching": [
                        {
                            "platform": "iOS",
                            "appVersionCode": 123,
                            "versionName": "String",
                            "appVersion": "2.0.0"
                        }
                    ],
                    "schedule": {
                        
                    },
                    "settings": {
                        "updateRecommended": true
                    }
                },
                {
                    "matching": [
                        {
                            "platform": "iOS",
                            "appVersion": "<3.0.0"
                        },
                        {
                            "platform": "Android",
                            "appVersionCode": "<123"
                        }
                    ],
                    "settings": {
                        "updateRequired": true
                    }
                }
            ],
            "meta": {
                "updated": "2024-01-08T12:00:00Z",
                "author": "Johan",
                "client": "Secret Agency"
            }
        }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)
        val settings = config.resolve(
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        val foo = settings["foo"] as Boolean
        assertEquals(false, foo)

        val bar = settings["bar"] as String
        assertEquals("hello world", bar)
    }

    @Test
    fun overridingWithAppVersion() {
        val jsonString = """
    {
        "settings": {
            "foo": 1
        },
        "overrides": [
            {
                "matching": [
                    {
                        "appVersion": "1.0.0"
                    }
                ],
                "settings": {
                    "foo": 2
                }
            }
        ]
    }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)
        val settings = config.resolve(
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        val foo = settings.getInt("foo")
        assertEquals(2, foo)
    }

    @Test
    fun overridingWithAppVersionRange() {
        val jsonString = """
    {
        "settings": {
            "foo": 1
        },
        "overrides": [
            {
                "matching": [
                    {
                        "appVersion": "0.7.0-1.0.0"
                    }
                ],
                "settings": {
                    "foo": 2
                }
            }
        ]
    }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.6.9"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(1, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.7.0"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(2, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.8.123"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(2, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(2, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.1"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(1, foo)
        }
    }

    @Test
    fun overridingWithMultipleOverrides() {
        val jsonString = """
    {
        "settings": {
            "foo": 1
        },
        "overrides": [
            {
                "matching": [
                    {
                        "appVersion": "0.7.0-1.0.0"
                    }
                ],
                "settings": {
                    "foo": 2
                }
            },
            {
                "matching": [
                    {
                        "appVersion": "1.0.0"
                    }
                ],
                "settings": {
                    "foo": 3
                }
            }
        ]
    }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.6.9"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(1, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.7.0"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(2, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.8.123"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(2, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(3, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.1"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(1, foo)
        }
    }

    @Test
    fun versionParsing() {
        try {
            val version = Version("1.0.0")
            assertEquals("1.0.0", version.rawValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val version = Version("1.0")
            assertEquals("1.0.0", version.rawValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val version = Version("1")
            assertEquals("1.0.0", version.rawValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val version = Version("1.0.0-test")
            assertEquals("1.0.0", version.rawValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val version = Version(" 1.0.0 ")
            assertEquals("1.0.0", version.rawValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun versionRangeParsing() {
        try {
            val versionRange = VersionRange.fromRawValue("1.0.0")
            assertEquals("1.0.0", versionRange.rawValue)
            assertFalse(versionRange.contains(Version("0.9.9")))
            assertTrue(versionRange.contains(Version("1.0.0")))
            assertFalse(versionRange.contains(Version("1.0.1")))
            assertFalse(versionRange.contains(Version("1.9.0")))
            assertFalse(versionRange.contains(Version("2.0.0")))
            assertFalse(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val versionRange = VersionRange.fromRawValue("1.0-2.0")
            assertEquals("1.0.0-2.0.0", versionRange.rawValue)
            assertFalse(versionRange.contains(Version("0.9.9")))
            assertTrue(versionRange.contains(Version("1.0.0")))
            assertTrue(versionRange.contains(Version("1.0.1")))
            assertTrue(versionRange.contains(Version("1.9.0")))
            assertTrue(versionRange.contains(Version("2.0.0")))
            assertFalse(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val versionRange = VersionRange.fromRawValue(">1")
            assertEquals(">1.0.0", versionRange.rawValue)
            assertFalse(versionRange.contains(Version("0.9.9")))
            assertFalse(versionRange.contains(Version("1.0.0")))
            assertTrue(versionRange.contains(Version("1.0.1")))
            assertTrue(versionRange.contains(Version("1.9.0")))
            assertTrue(versionRange.contains(Version("2.0.0")))
            assertTrue(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val versionRange = VersionRange.fromRawValue("<=1.0.0")
            assertEquals("<=1.0.0", versionRange.rawValue)
            assertTrue(versionRange.contains(Version("0.9.9")))
            assertTrue(versionRange.contains(Version("1.0.0")))
            assertFalse(versionRange.contains(Version("1.0.1")))
            assertFalse(versionRange.contains(Version("1.9.0")))
            assertFalse(versionRange.contains(Version("2.0.0")))
            assertFalse(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val versionRange = VersionRange.fromRawValue("1.0.0>-<2.0.0")
            assertEquals("1.0.0>-<2.0.0", versionRange.rawValue)
            assertFalse(versionRange.contains(Version("0.9.9")))
            assertFalse(versionRange.contains(Version("1.0.0")))
            assertTrue(versionRange.contains(Version("1.0.1")))
            assertTrue(versionRange.contains(Version("1.9.0")))
            assertFalse(versionRange.contains(Version("2.0.0")))
            assertFalse(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val versionRange = VersionRange.fromRawValue("1.0.0>-2.0.0")
            assertEquals("1.0.0>-2.0.0", versionRange.rawValue)
            assertFalse(versionRange.contains(Version("0.9.9")))
            assertFalse(versionRange.contains(Version("1.0.0")))
            assertTrue(versionRange.contains(Version("1.0.1")))
            assertTrue(versionRange.contains(Version("1.9.0")))
            assertTrue(versionRange.contains(Version("2.0.0")))
            assertFalse(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val versionRange = VersionRange.fromRawValue("1.0.0-<2.0.0")
            assertEquals("1.0.0-<2.0.0", versionRange.rawValue)
            assertFalse(versionRange.contains(Version("0.9.9")))
            assertTrue(versionRange.contains(Version("1.0.0")))
            assertTrue(versionRange.contains(Version("1.0.1")))
            assertTrue(versionRange.contains(Version("1.9.0")))
            assertFalse(versionRange.contains(Version("2.0.0")))
            assertFalse(versionRange.contains(Version("2.0.1")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun notMatchingWhenUnknownKeysArePresent() {
        val jsonString = """
        {
            "settings": {
                "foo": 1
            },
            "overrides": [
                {
                    "matching": [
                        {
                            "appVersion": "1.0.0",
                            "unknownKey": "present"
                        }
                    ],
                    "settings": {
                        "foo": 2
                    }
                }
            ]
        }
        """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)
        val settings = config.resolve(
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        val foo = settings.getInt("foo")
        assertEquals(1, foo)
    }

    @Test
    fun relevantDates() {
        val jsonString = """
    {
        "settings": {
            "foo": 1
        },
        "overrides": [
            {
                "matching": [
                    {
                        "appVersion": "1.0.0"
                    }
                ],
                "schedule": {
                    "from": "2024-08-21T00:00:00Z",
                    "until": "2024-09-11T00:00:00Z"
                },
                "settings": {
                    "foo": 2
                }
            }
        ]
    }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)
        val dates = config.relevantResolutionDates(
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        val expectedDates = listOf(
            dateFormatter.parse("2024-08-21T00:00:00Z"),
            dateFormatter.parse("2024-09-11T00:00:00Z")
        )

        assertEquals(expectedDates, dates)
    }

    @Test
    fun overridingWithABuildVariant() {
        val jsonString = """
    {
        "settings": {
            "foo": 1
        },
        "overrides": [
            {
                "matching": [
                    {
                        "buildVariant": "debug"
                    }
                ],
                "settings": {
                    "foo": 2
                }
            }
        ]
    }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.DEBUG
            )
            val foo = settings.getInt("foo")
            assertEquals(2, foo)
        }

        runCatching {
            val settings = config.resolve(
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.RELEASE
            )
            val foo = settings.getInt("foo")
            assertEquals(1, foo)
        }
    }

    @Test
    fun overridingWithAnUnsupportedBuildVariant() {
        val jsonString = """
    {
        "settings": {
            "foo": 1
        },
        "overrides": [
            {
                "matching": [
                    {
                        "buildVariant": "unsupported variant"
                    }
                ],
                "settings": {
                    "foo": 2
                }
            }
        ]
    }
    """
        val json = JSONObject(jsonString)

        val date = Date(0)
        val config = Config(json)
        val settings = config.resolve(
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )
        val foo = settings.getInt("foo")
        assertEquals(1, foo)
    }
}