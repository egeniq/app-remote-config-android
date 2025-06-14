package com.egeniq.appremoteconfig

import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ConditionSerializerTest {
    @Test
    fun parsesCondition() {
        val json = """
            {
              "variant": "free"
            }
        """.trimIndent()

        val condition = Json.decodeFromString<Condition>(ConditionSerializer, json)

        assertEquals(Condition(variant = "free"), condition)
    }

    @Test
    fun `Never matches for unknown properties`() {
        val json = """
            {
              "variant": "free",
              "newProp": true
            }
        """.trimIndent()

        val condition = Json.decodeFromString<Condition>(ConditionSerializer, json)

        assertEquals(Condition(), condition)
    }
}