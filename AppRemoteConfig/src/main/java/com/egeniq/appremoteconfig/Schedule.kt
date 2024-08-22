package com.egeniq.appremoteconfig

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class Schedule(
    var matchNever: Boolean,
    var from: Instant?,
    var until: Instant?,
) {
    constructor(json: JsonObject) : this(
        matchNever = false,
        from = null,
        until = null
    ) {
        if (json.containsKey("from")) {
            try {
                val fromString = json["from"]?.jsonPrimitive?.contentOrNull
                val date = fromString?.let { Instant.parse(it) }
                from = date
            } catch (e: Exception) {
                matchNever = true
                from = null
                until = null
                return
            }
        } else {
            from = null
        }

        if (json.containsKey("until")) {
            try {
                val untilString = json["until"]?.jsonPrimitive?.contentOrNull
                val date = untilString?.let { Instant.parse(it) }
                until = date
            } catch (e: Exception) {
                matchNever = true
                until = null
                return
            }
        } else {
            until = null
        }

        matchNever = false
    }

    fun contains(date: Instant): Boolean {
        val from = from
        val until = until
        val matchNever = matchNever

        if (matchNever) {
            return false
        }
        val validFrom = from?.let {
            date < it
        }
        val validUntil = until?.let {
            date >= it
        }

        if (from != null && date < from) {
            return false
        }
        if (until != null && date.compareTo(until) >= 0) {
            return false
        }
        return true
    }
}
