package com.egeniq.appremoteconfig

import kotlinx.datetime.Instant
import org.json.JSONObject

data class Schedule(
    var matchNever: Boolean,
    var from: Instant?,
    var until: Instant?
) {
    constructor(json: JSONObject) : this(
        matchNever = false,
        from = null,
        until = null
    ) {
        if (json.has("from")) {
            try {
                val fromString = json.getString("from")
                val date = Instant.parse(fromString)
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

        if (json.has("until")) {
            try {
                val fromString = json.getString("until")
                val date = Instant.parse(fromString)
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

        if (matchNever) {
            return false
        }
        if (from != null && date.compareTo(from) < 0) {
            return false
        }
        if (until != null && date.compareTo(until) >= 0) {
            return false
        }
        return true
    }
}
