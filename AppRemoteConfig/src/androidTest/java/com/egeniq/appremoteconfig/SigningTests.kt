package com.egeniq.appremoteconfig

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import kotlinx.datetime.Instant
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SigningTests {

    private val sodiumClient = LazySodiumAndroid(SodiumAndroid())

    @Test
    fun verifying() {
        val publicKey = "rK21qYyxsj8x75kCqU8k99zU4bEJdI60fMTPzsviBtE="
        val secretKey =
            "7/z5fFbEF1xPlCWJdqhfd6SV5lp/xusUsc7VpYTAX2asrbWpjLGyPzHvmQKpTyT33NThsQl0jrR8xM/Oy+IG0Q=="
        val configString = """
            {"settings": {"testing": true}}
            """

        val signedConfigString =
            sodiumClient.cryptoSign(configString, Key.fromBase64String(secretKey))
        val signedConfig = Config.new(signedConfigString, publicKey)
        val date = Instant.fromEpochMilliseconds(0)
        val settings = signedConfig.resolve(
            date = date,
            platform = Platform.IOS_IPHONE,
            platformVersion = OperatingSystemVersion(16, 0, 1),
            appVersion = Version("1.0.0"),
            variant = "AppStore",
            buildVariant = BuildVariant.RELEASE
        )
        val testing = settings["testing"] as Boolean
        assertEquals(true, testing)
    }

    @Test
    fun verifyingWithIncorrectPublicKey() {
        val publicKey = "incorrectkey/5kCqU8k99zU4bEJdI60fMTPzsviBtE="
        val secretKey =
            "7/z5fFbEF1xPlCWJdqhfd6SV5lp/xusUsc7VpYTAX2asrbWpjLGyPzHvmQKpTyT33NThsQl0jrR8xM/Oy+IG0Q=="
        val configString = """
            {"settings": {"testing": true}}
            """

        val signedConfigString = sodiumClient.cryptoSign(configString, Key.fromBase64String(secretKey))

        assertThrows(ConfigError.InvalidSignature::class.java) {
            val signedConfig = Config.new(signedConfigString, publicKey)
        }
    }

    @Test
    fun verifyingWithInvalidSignature() {
        val publicKey = "rK21qYyxsj8x75kCqU8k99zU4bEJdI60fMTPzsviBtE="
        val secretKey =
            "7/z5fFbEF1xPlCWJdqhfd6SV5lp/xusUsc7VpYTAX2asrbWpjLGyPzHvmQKpTyT33NThsQl0jrR8xM/Oy+IG0Q=="
        val configString = """
                {"settings": {"testing": true}}
                """
        val signedConfigString =
            sodiumClient.cryptoSign(configString, Key.fromBase64String(secretKey))
        // Invalidate signature
        val invalidConfigString = signedConfigString.replaceRange(0, 1, "1")
        assertThrows(ConfigError.InvalidSignature::class.java) {
            val signedConfig = Config.new(invalidConfigString, publicKey)
        }
    }
}