package com.egeniq.appremoteconfig
import android.annotation.SuppressLint
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

@Serializable
data class Schedule(
    var matchNever: Boolean,
    var from: Instant?,
    var until: Instant?
) {
    companion object {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // Quoted "Z" to indicate UTC, no timezone offset is weird!
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

    fun contains(date: Date): Boolean {
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
