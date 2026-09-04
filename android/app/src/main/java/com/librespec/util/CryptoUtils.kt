package com.librespec.util

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val SECRET_KEY = "default_secure_key_123"

    fun calculateHmac(vector: List<Int>, timestamp: Long): String {
        val vectorJson = vector.joinToString(prefix = "[", postfix = "]", separator = ",")
        val payloadString = "$vectorJson|$timestamp"
        
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        
        val hashBytes = mac.doFinal(payloadString.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun generateAppSideHash(baseline: List<Int>, plateau: List<Int>, timestamp: Long): String {
        val payloadString = "${baseline.joinToString(",")}|${plateau.joinToString(",")}|$timestamp"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(payloadString.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
