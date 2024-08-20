package com.egeniq.appremoteconfig

import kotlinx.serialization.json.JsonObject

fun <T> JsonObject.get(key: String): T? =
    if (this.containsKey(key)) {
        this[key] as T
    } else {
        null
    }
