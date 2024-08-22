package com.egeniq.appremoteconfig

sealed class ConfigError : Exception() {
    class NonSemanticVersion : ConfigError()
    class InvalidVersionRange : ConfigError()
    class UnexpectedTypeForKey : ConfigError()
}