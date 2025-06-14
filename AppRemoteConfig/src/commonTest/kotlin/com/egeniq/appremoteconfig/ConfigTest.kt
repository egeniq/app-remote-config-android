package com.egeniq.appremoteconfig

import junit.framework.TestCase.assertEquals
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.junit.Test

class ConfigTest {

    @Serializable
    @JsonIgnoreUnknownKeys
    data class TestFooIntSetting(val foo: Int)

    @Test
    fun `parses config from string`() {
        @Serializable
        @JsonIgnoreUnknownKeys
        data class Settings(val foo: Boolean, val bar: String)

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
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            Settings.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            variant = "AppStore",
            buildVariant = BuildVariant.RELEASE
        )


        assertEquals(false, settings.foo)
        assertEquals("hello world", settings.bar)
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
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        assertEquals(2, settings.foo)
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

        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)

        assertEquals(
            1, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.6.9"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )

        assertEquals(
            2, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.7.0"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )

        assertEquals(
            2, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.8.123"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )


        assertEquals(
            2, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )


        assertEquals(
            1, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.1"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )
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
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)

        assertEquals(
            1, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.6.9"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )

        assertEquals(
            2, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.7.0"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )

        assertEquals(
            2, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("0.8.123"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )


        assertEquals(
            3, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )

        assertEquals(
            1, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.1"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )
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

        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )
        assertEquals(1, settings.foo)
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
        val config = Config(jsonString)
        val dates = config.relevantResolutionDates(
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        val expectedDates = listOf(
            Instant.parse("2024-08-21T00:00:00Z"),
            Instant.parse("2024-09-11T00:00:00Z")
        )

        assertEquals(expectedDates, dates)
    }

    @Test
    fun relevantDatesWithOtherZones() {
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
                        "from": "2024-08-21T00:00:00+01:00",
                        "until": "2024-09-11T00:00:00-09:00"
                    },
                    "settings": {
                        "foo": 2
                    }
                }
            ]
        }
        """

        val config = Config(jsonString)
        val dates = config.relevantResolutionDates(
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        val expectedDates = listOf(
            Instant.parse("2024-08-20T23:00:00Z"),
            Instant.parse("2024-09-11T09:00:00Z")
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
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)

        assertEquals(
            2, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.DEBUG
            ).foo
        )


        assertEquals(
            1, config.resolve(
                TestFooIntSetting.serializer(),
                date = date,
                platform = Platform.IOS_IPHONE,
                platformVersion = OperatingSystemVersion(16, 0, 1),
                appVersion = Version("1.0.0"),
                buildVariant = BuildVariant.RELEASE
            ).foo
        )
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
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )
        assertEquals(1, settings.foo)
    }

    @Test
    fun overridingWithInvalidKeys() {
        val jsonString = """
        {
            "settings": {
                "foo": 1
            },
            "overrides": [
                {
                    "matching": [
                        {
                            "buildVariant": "unsupported variant",
                            "platform": true
                        }
                    ],
                    "settings": {
                        "foo": 2
                    }
                }
            ]
        }
        """

        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        assertEquals(1, settings.foo)
    }

    @Test
    fun overridingWithUnknownPlatform() {
        val jsonString = """
        {
            "settings": {
                "foo": 1
            },
            "overrides": [
                {
                    "matching": [
                        {
                            "platform": "unsupported platform"
                        }
                    ],
                    "settings": {
                        "foo": 2
                    }
                }
            ]
        }
        """

        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        assertEquals(1, settings.foo)
    }

    @Test
    fun overridingWithUnsupportedKey() {
        val jsonString = """
        {
            "settings": {
                "foo": 1
            },
            "overrides": [
                {
                    "matching": [
                        {
                            "unsupported key": "unsupported value"
                        }
                    ],
                    "settings": {
                        "foo": 2
                    }
                }
            ]
        }
        """
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )

        assertEquals(1, settings.foo)
    }

    @Test
    fun overridingWithUnsupportedAppVersion() {
        val jsonString = """
        {
            "settings": {
                "foo": 1
            },
            "overrides": [
                {
                    "matching": [
                        {
                            "appVersion": "unsupported value"
                        }
                    ],
                    "settings": {
                        "foo": 2
                    }
                }
            ]
        }
        """
        val date = Instant.fromEpochMilliseconds(0)
        val config = Config(jsonString)
        val settings = config.resolve(
            TestFooIntSetting.serializer(),
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            buildVariant = BuildVariant.RELEASE
        )
        assertEquals(1, settings.foo)
    }
}
