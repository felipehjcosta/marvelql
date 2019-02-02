package com.github.felipehjcosta.adapters.infrastructure

fun String.toMD5(): String {
    val messageDigest = java.security.MessageDigest.getInstance("MD5")
    val digested = messageDigest.digest(toByteArray())
    return digested.joinToString("") { String.format("%02x", it) }
}
