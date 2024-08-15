package com.egeniq.appremoteconfig

//enum class ConfigError(val message: String) : Throwable(message) {
//    NON_SEMANTIC_VERSION("Non-semantic version"),
//    INVALID_VERSION_RANGE("Invalid version range"),
//    UNEXPECTED_TYPE_FOR_KEY("Unexpected type for key");
//}

sealed class ConfigError : Exception() {
    class NonSemanticVersion : ConfigError()
    class InvalidVersionRange : ConfigError()
    class UnexpectedTypeForKey : ConfigError()
}