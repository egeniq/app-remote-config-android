package com.egeniq.appremoteconfig

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AppRemoteConfigTests {

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
            platform = Platform.iOS_iPhone,
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
}                                                                                                                                                                                                                              }
