package cn.netdiscovery.adbd.utils

import io.netty.buffer.Unpooled
import io.netty.util.ReferenceCountUtil
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.Security
import java.security.interfaces.RSAPrivateCrtKey

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.utils.AuthUtil
 * @author: Tony Shen
 * @date: 2022/4/6 10:52 下午
 * @version: V1.0 <描述当前版本功能>
 */
object AuthUtil {

    private val ASN1_PREAMBLE = byteArrayOf(0x00, 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00, 0x04, 0x14)
    private const val B64MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    private const val B64PAD = "="

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Throws(IOException::class)
    fun loadPrivateKey(path: String?): RSAPrivateCrtKey {
        val bytes: ByteArray = readAll(path)
        val pemParser = PEMParser(InputStreamReader(ByteArrayInputStream(bytes)))
        val `object` = pemParser.readObject()
        val converter = JcaPEMKeyConverter().setProvider("BC")
        return converter.getPrivateKey(`object` as PrivateKeyInfo) as RSAPrivateCrtKey
    }

    private fun hex2b64(h: String): String {
        var i: Int
        var c: Int
        val builder = StringBuilder()
        i = 0
        while (i + 3 <= h.length) {
            c = Integer.valueOf(h.substring(i, i + 3), 16)
            builder.append(B64MAP[c shr 6])
            builder.append(B64MAP[c and 63])
            i += 3
        }
        if (i + 1 == h.length) {
            c = Integer.valueOf(h.substring(i, i + 1), 16)
            builder.append(B64MAP[c shl 2])
        } else if (i + 2 == h.length) {
            c = Integer.valueOf(h.substring(i, i + 2), 16)
            builder.append(B64MAP[c shr 2])
            builder.append(B64MAP[c and 3 shl 4])
        }
        while (builder.length and 3 > 0) {
            builder.append(B64PAD)
        }
        return builder.toString()
    }

    fun generatePublicKey(privateKey: RSAPrivateCrtKey): String {
        val numWords = privateKey.modulus.bitLength() / 32
        val B32 = BigInteger.ONE.shiftLeft(32)
        var N = BigInteger(privateKey.modulus.toByteArray())
        val R = BigInteger.ONE.shiftLeft(1).pow(privateKey.modulus.bitLength())
        var RR = R.multiply(R).mod(N)
        val capacity = (3 + numWords * 2) * 4
        val buffer = Unpooled.buffer(capacity, capacity)
        return try {
            buffer.setIntLE(0, numWords)
            buffer.setIntLE(4, B32.subtract(N.modInverse(B32)).toInt())
            run {
                var i = 2
                var j = 2 + numWords
                while (i < numWords + 2) {
                    buffer.setIntLE(i * 4, N.mod(B32).toInt())
                    N = N.divide(B32)
                    buffer.setIntLE(j * 4, RR.mod(B32).toInt())
                    RR = RR.divide(B32)
                    ++i
                    ++j
                }
            }
            buffer.setIntLE(capacity - 4, privateKey.publicExponent.toInt())
            val builder = StringBuilder()
            for (i in 0 until capacity) {
                val digit = Integer.toHexString(buffer.getUnsignedByte(i).toInt())
                if (digit.length == 1) {
                    builder.append("0")
                }
                builder.append(digit)
            }
            hex2b64(builder.toString()) + " adbs@adbs"
        } finally {
            ReferenceCountUtil.safeRelease(buffer)
        }
    }

    fun sign(privateKey: RSAPrivateCrtKey, data: ByteArray): BigInteger {
        val totalLen = privateKey.modulus.bitLength() / 8
        val buffer = Unpooled.buffer(totalLen, totalLen)
        return try {
            buffer.setByte(0, 0x00)
            buffer.setByte(1, 0x01)
            var padEnd: Int = totalLen - ASN1_PREAMBLE.size - data.size
            for (i in 2 until padEnd) {
                buffer.setByte(i, 0xFF.toByte().toInt())
            }
            for (i in ASN1_PREAMBLE.indices) {
                buffer.setByte(padEnd + i, ASN1_PREAMBLE[i].toInt())
            }
            padEnd += ASN1_PREAMBLE.size
            for (i in data.indices) {
                buffer.setByte(padEnd + i, data[i].toInt())
            }
            var x = BigInteger.ZERO
            for (i in 0 until totalLen) {
                val c = BigInteger.valueOf(buffer.getUnsignedByte(i).toLong())
                x = x.add(c.shiftLeft((totalLen - i - 1) * 8))
            }
            doPrivate(privateKey, x)
        } finally {
            ReferenceCountUtil.safeRelease(buffer)
        }
    }

    private fun doPrivate(privateKey: RSAPrivateCrtKey, x: BigInteger): BigInteger {
        val p = privateKey.primeP
        val q = privateKey.primeQ
        val d = privateKey.privateExponent
        val n = privateKey.modulus
        val dmp1 = privateKey.primeExponentP
        val dmq1 = privateKey.primeExponentQ
        val coeff = privateKey.crtCoefficient
        if (p == null || q == null) return x.modPow(d, n)
        var xp = x.mod(p).modPow(dmp1, p)
        val xq = x.mod(q).modPow(dmq1, q)
        while (xp.compareTo(xq) < 0) xp = xp.add(p)
        return xp.subtract(xq).multiply(coeff).mod(p).multiply(q).add(xq)
    }
}
