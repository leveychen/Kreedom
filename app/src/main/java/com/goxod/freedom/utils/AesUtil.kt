package com.goxod.freedom.utils

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesUtil {

    private const val IV = "XY3MUCwdQJblANm3"
    private const val PASSWORD = "BWbl60TRUdmUbJ49"
    private const val SALT = "oyfDYZIDTRmRp8JD"
    private const val FAILED = "FAILED"

    fun decrypt(encrypted: String): String {
        return try {
            val decodedValue = Base64.decode(getBytes(encrypted), Base64.DEFAULT)
            val c = getCipher(Cipher.DECRYPT_MODE)
            val decValue = c.doFinal(decodedValue)
            String(decValue)
        } catch (e: Exception) {
            e.printStackTrace()
            FAILED
        }
    }

    fun encrypt(raw: String): String {
        return try {
            val c = getCipher(Cipher.ENCRYPT_MODE)
            val encryptedVal = c.doFinal(getBytes(raw))
            String(Base64.encode(encryptedVal,Base64.DEFAULT))
        } catch (t: Throwable) {
            t.printStackTrace()
            FAILED
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getBytes(str: String): ByteArray {
        return str.toByteArray(charset("UTF-8"))
    }

    @Throws(Exception::class)
    private fun getCipher(mode:Int): Cipher {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(mode, generateKey(), IvParameterSpec(getBytes(IV)))
        return c
    }

    @Throws(Exception::class)
    private fun generateKey(): Key {
        val spec = PBEKeySpec(PASSWORD.toCharArray(), getBytes(SALT), 65536, 128)
        val tmp = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}