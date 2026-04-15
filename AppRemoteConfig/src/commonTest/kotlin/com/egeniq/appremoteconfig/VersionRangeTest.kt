package com.egeniq.appremoteconfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionRangeTest {
    @Test
    fun `Parses exact version range`() {
        val versionRange = VersionRange.fromRawValue("1.0.0")
        assertEquals(VersionRange.Equal(Version("1.0.0")), versionRange)
        assertFalse(versionRange.contains(Version("0.9.9")))
        assertTrue(versionRange.contains(Version("1.0.0")))
        assertFalse(versionRange.contains(Version("1.0.1")))
        assertFalse(versionRange.contains(Version("1.9.0")))
        assertFalse(versionRange.contains(Version("2.0.0")))
        assertFalse(versionRange.contains(Version("2.0.1")))
    }

    @Test
    fun `Parses inclusive version range`() {
        val versionRange = VersionRange.fromRawValue("1.0-2.0")
        assertEquals(
            VersionRange.Between(Version("1.0.0") to true, Version("2.0.0") to true),
            versionRange
        )
        assertFalse(versionRange.contains(Version("0.9.9")))
        assertTrue(versionRange.contains(Version("1.0.0")))
        assertTrue(versionRange.contains(Version("1.0.1")))
        assertTrue(versionRange.contains(Version("1.9.0")))
        assertTrue(versionRange.contains(Version("2.0.0")))
        assertFalse(versionRange.contains(Version("2.0.1")))
    }

    @Test
    fun `Parses greater then exclusive range`() {
        val versionRange = VersionRange.fromRawValue(">1")
        assertEquals(VersionRange.GreaterThan(Version("1.0.0"), false), versionRange)
        assertFalse(versionRange.contains(Version("0.9.9")))
        assertFalse(versionRange.contains(Version("1.0.0")))
        assertTrue(versionRange.contains(Version("1.0.1")))
        assertTrue(versionRange.contains(Version("1.9.0")))
        assertTrue(versionRange.contains(Version("2.0.0")))
        assertTrue(versionRange.contains(Version("2.0.1")))
    }

    @Test
    fun `Parses lesser than inclusive range`() {
        val versionRange = VersionRange.fromRawValue("<=1.0.0")
        assertEquals(VersionRange.LesserThan(Version("1.0.0"), inclusive = true), versionRange)
        assertTrue(versionRange.contains(Version("0.9.9")))
        assertTrue(versionRange.contains(Version("1.0.0")))
        assertFalse(versionRange.contains(Version("1.0.1")))
        assertFalse(versionRange.contains(Version("1.9.0")))
        assertFalse(versionRange.contains(Version("2.0.0")))
        assertFalse(versionRange.contains(Version("2.0.1")))
    }

    @Test
    fun `Parses between exclusive range`() {
        val versionRange = VersionRange.fromRawValue("1.0.0>-<2.0.0")
        assertEquals(
            VersionRange.Between(Version("1.0.0") to false, Version("2.0.0") to false),
            versionRange
        )
        assertFalse(versionRange.contains(Version("0.9.9")))
        assertFalse(versionRange.contains(Version("1.0.0")))
        assertTrue(versionRange.contains(Version("1.0.1")))
        assertTrue(versionRange.contains(Version("1.9.0")))
        assertFalse(versionRange.contains(Version("2.0.0")))
        assertFalse(versionRange.contains(Version("2.0.1")))
    }

    @Test
    fun `Parses between exclusive inclusive range`() {
        val versionRange = VersionRange.fromRawValue("1.0.0>-2.0.0")
        assertEquals(
            VersionRange.Between(Version("1.0.0") to false, Version("2.0.0") to true),
            versionRange
        )
        assertFalse(versionRange.contains(Version("0.9.9")))
        assertFalse(versionRange.contains(Version("1.0.0")))
        assertTrue(versionRange.contains(Version("1.0.1")))
        assertTrue(versionRange.contains(Version("1.9.0")))
        assertTrue(versionRange.contains(Version("2.0.0")))
        assertFalse(versionRange.contains(Version("2.0.1")))
    }

    @Test
    fun `Parses between inclusive exclusive range`() {
        val versionRange = VersionRange.fromRawValue("1.0.0-<2.0.0")
        assertEquals(
            VersionRange.Between(Version("1.0.0") to true, Version("2.0.0") to false),
            versionRange
        )
        assertFalse(versionRange.contains(Version("0.9.9")))
        assertTrue(versionRange.contains(Version("1.0.0")))
        assertTrue(versionRange.contains(Version("1.0.1")))
        assertTrue(versionRange.contains(Version("1.9.0")))
        assertFalse(versionRange.contains(Version("2.0.0")))
        assertFalse(versionRange.contains(Version("2.0.1")))
    }
}