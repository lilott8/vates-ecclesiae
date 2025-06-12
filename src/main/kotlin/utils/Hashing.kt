package us.cedarfarm.utils

import kotlinx.io.files.Path
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest


fun String.toSHA256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(64, '0')
}

fun Path.toSha256(): String? {
    val file = File(this.toString())
    if (!file.exists() || !file.isFile) {
        return null
    }

    val messageDigest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192) // 8KB buffer
    var bytesRead: Int

    FileInputStream(file).use { fis ->
        while (fis.read(buffer).also { bytesRead = it } != -1) {
            messageDigest.update(buffer, 0, bytesRead)
        }
    }

    val hashBytes = messageDigest.digest()
    return hashBytes.joinToString("") { "%02x".format(it) }
}