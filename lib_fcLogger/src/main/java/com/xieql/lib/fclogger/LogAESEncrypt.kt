package com.xieql.lib.fclogger

import android.util.Base64
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

//日志加密解密
internal object LogAESEncrypt {

    private const val TAG = "LogAESEncrypt"

    //加密算法
    private const val KEY_ALGORITHM = "AES"
    //AES 的 密钥长度，32 字节，范围：16 - 32 字节
    private const val SECRET_KEY_LENGTH = 32
    //字符编码
    private val CHARSET_UTF8: Charset = StandardCharsets.UTF_8
    //秘钥长度不足 16 个字节时，默认填充位数
    private const val DEFAULT_VALUE = "0"
    //加解密算法/工作模式/填充方式
    private const val CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding"
    //默认秘钥
    private const val DEFAULT_KEY = "FC123456FC"

    /**
     * AES 加密
     *
     * @param data      待加密内容
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @return 返回Base64转码后的加密数据
     */
    fun encrypt(data: String, secretKey: String= DEFAULT_KEY): String? {
        try {
            //创建密码器
            val cipher: Cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            //初始化为加密密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey))
            val encryptByte: ByteArray = cipher.doFinal(data.toByteArray(CHARSET_UTF8))
            //将加密以后的数据进行 Base64 编码
            return base64Encode(encryptByte)
        } catch (e: Exception) {
            LogUtils.e(TAG, "加密失败", Exception("日志加密失败"))
        }
        return null
    }

    /**
     * AES 解密
     *
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     */
    fun decrypt(base64Data: String, secretKey: String= DEFAULT_KEY): String? {
        try {
            val data = base64Decode(base64Data)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            //设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey!!))
            //执行解密操作
            val result = cipher.doFinal(data)
            return String(result, CHARSET_UTF8)
        } catch (e: Exception) {
            LogUtils.e(TAG, "解密失败", Exception("日志解密失败"))
        }
        return null
    }


    //使用密码获取 AES 秘钥
    private fun getSecretKey(secretKey: String): SecretKeySpec? {
        var secretKey = secretKey
        secretKey = toMakeKey(secretKey, SECRET_KEY_LENGTH, DEFAULT_VALUE)
        return SecretKeySpec(secretKey.toByteArray(CHARSET_UTF8), KEY_ALGORITHM)
    }

    /**
     * 如果 AES 的密钥小于 `length` 的长度，就对秘钥进行补位，保证秘钥安全。
     *
     * @param secretKey 密钥 key
     * @param length    密钥应有的长度
     * @param text      默认补的文本
     * @return 密钥
     */
    private fun toMakeKey(secretKey: String, length: Int, text: String): String{
        // 获取密钥长度
        var secretKey = secretKey
        val strLen = secretKey.length
        // 判断长度是否小于应有的长度
        if (strLen < length) {
            // 补全位数
            val builder = StringBuilder()
            // 将key添加至builder中
            builder.append(secretKey)
            // 遍历添加默认文本
            for (i in 0 until length - strLen) {
                builder.append(text)
            }
            // 赋值
            secretKey = builder.toString()
        }
        return secretKey
    }

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    private fun base64Decode(data: String?): ByteArray? {
        return Base64.decode(data, Base64.NO_WRAP)
    }

    /**
     * 将 字节数组 转换成 Base64 编码
     */
    private fun base64Encode(data: ByteArray?): String? {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }



}

fun logDecrty() {
    val a = "UbnY3W9YmKNbOkympu9priVt4jBCjYVS8cGLmUEnDS1DfdK351U63EjMV7BH6/gJFo/EfcHuHtpJbokHnLftVHb4+at5HVX+hwbxPhVfv9nwdYaYyoRd2a73qTEUxEU8pe79dPbewj4eDVnxhhBAWzR+XURFxI37ZkF/YmBi+HQsEo8ajTtphI8R+GYQeFbSPdJZ0PQycjB8uxWPwtGs4yV6QGNIybjQKryLnmko20JRwekmWp49WDrus20ep7sLEpd5Zurb7M6ld26KSX6p4TkveadT3LAI9Q5LmCQ6zC3Emi0RTSxawbNW7FGGxS6Q1iB8cod9v5P+UZO/3/Dx0wMslhKe87tlLfxUhAfFtrNubns+FWX850AGJRdrWsD3gwQsW2IcaQmjFOWBAhqzPf4pG+CO6U6P9DywMK5/YVRHaZDPLmIxHBhXZcOpCZX3"

    LogUtils.i("日志解密", LogAESEncrypt.decrypt(a))
}
