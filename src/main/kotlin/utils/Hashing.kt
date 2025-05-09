package us.cedarfarm.utils

import java.math.BigInteger
import java.security.MessageDigest


fun String.toSHA256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(64, '0')
}