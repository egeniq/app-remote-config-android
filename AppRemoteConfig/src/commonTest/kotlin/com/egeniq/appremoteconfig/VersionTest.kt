package com.egeniq.appremoteconfig

import kotlin.test.Test
import kotlin.test.assertEquals

class VersionTest {
    @Test
    fun `Parses string version`() {
        assertEquals(Triple(1, 0, 0), Version("1.0.0").canonical)
        assertEquals(Triple(1, 0, 0), Version("1.0").canonical)
        assertEquals(Triple(1, 0, 0), Version("1").canonical)
        assertEquals(Triple(1, 0, 0), Version("1.0.0-test").canonical)
        assertEquals(Triple(1, 0, 0), Version("1.0.0 ").canonical)
    }
}