package com.egeniq.appremoteconfig
import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

data class Schedule(
    var matchNever: Boolean,
    var from: Date?,
    var until: Date?
) {
    companion object {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    constructor(json: JSONObject) : this(
        matchNever = false,
        from = null,
        until = null
    ) {
        val fromJSON: Any? = json["from"]
        if (fromJSON != null) {
            val fromString = fromJSON as? String
            if (fromString != null) {
                val date = dateFormatter.parse(fromString)
                if (date != null) {
                    from = date
                }  else {
                    matchNever = true
                    from = null
                    until = null
                    return
                }
            } else {
                matchNever = true
                from = null
                until = null
                return
            }
        } else {
            from = null
        }

        val untilJSON: Any? = json["until"]
        if (untilJSON != null) {
            val untilString = untilJSON as? String
            if (untilString != null) {
                val date = dateFormatter.parse(untilString)
                if (date != null) {
                    until = date
                } else {
                    matchNever = true
                    until = null
                    return
                }
            } else {
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
