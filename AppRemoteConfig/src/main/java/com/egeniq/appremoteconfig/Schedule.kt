package com.egeniq.appremoteconfig

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.text.SimpleDateFormat

@Serializable
data class Schedule(
    var matchNever: Boolean,
    var from: Instant?,
    var until: Instant?,
) {
    companion object {
        val dateFormatter =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // Quoted "Z" to indicate UTC, no timezone offset is weird!
    }

    constructor(json: JSONObject) : this(
        matchNever = false,
        from = null,
        until = null
    ) {
        if (json.has("from")) {
            try {
                val fromString = json.getString("from")
                val date = dateFormatter.parse(fromString)
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
                val date = dateFormatter.parse(fromString)
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
