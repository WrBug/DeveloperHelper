package com.wrbug.developerhelper.commonutil

object Base64 {

    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
        .toCharArray()
    private val codes = ByteArray(256)
    /* 将原始数据编码为base64编码
     */
    fun encodeAsString(data: ByteArray): String {
        return String(encode(data))
    }

    fun encode(data: ByteArray): CharArray {
        val out = CharArray((data.size + 2) / 3 * 4)
        var i = 0
        var index = 0
        while (i < data.size) {
            var quad = false
            var trip = false
            var value = 0xFF and data[i].toInt()
            value = value shl 8
            if (i + 1 < data.size) {
                value = value or (0xFF and data[i + 1].toInt())
                trip = true
            }
            value = value shl 8
            if (i + 2 < data.size) {
                value = value or (0xFF and data[i + 2].toInt())
                quad = true
            }
            out[index + 3] = alphabet[if (quad) value and 0x3F else 64]
            value = value shr 6
            out[index + 2] = alphabet[if (trip) value and 0x3F else 64]
            value = value shr 6
            out[index + 1] = alphabet[value and 0x3F]
            value = value shr 6
            out[index + 0] = alphabet[value and 0x3F]
            i += 3
            index += 4
        }
        return out
    }

    /**
     * 将base64编码的数据解码成原始数据
     */
    fun decode(data: CharArray): ByteArray {
        var len = (data.size + 3) / 4 * 3
        if (data.size > 0 && data[data.size - 1] == '=')
            --len
        if (data.size > 1 && data[data.size - 2] == '=')
            --len
        val out = ByteArray(len)
        var shift = 0
        var accum = 0
        var index = 0
        for (ix in data.indices) {
            val value = codes[data[ix].toInt() and 0xFF].toInt()
            if (value >= 0) {
                accum = accum shl 6
                shift += 6
                accum = accum or value
                if (shift >= 8) {
                    shift -= 8
                    out[index++] = (accum shr shift and 0xff).toByte()
                }
            }
        }
        if (index != out.size)
            throw Error("miscalculated data length!")
        return out
    }

    fun decode(data: String?): String {
        if (data.isNullOrEmpty()) {
            return ""
        }
        return String(decode(data.toCharArray()))
    }

    init {
        for (i in 0..255)
            codes[i] = -1
        run {
            var i: Int = 'A'.toInt()
            while (i <= 'Z'.toInt()) {
                codes[i] = (i - 'A'.toInt()).toByte()
                i++
            }
        }
        run {
            var i: Int = 'a'.toInt()
            while (i <= 'z'.toInt()) {
                codes[i] = (26 + i - 'a'.toInt()).toByte()
                i++
            }
        }
        var i: Int = '0'.toInt()
        while (i <= '9'.toInt()) {
            codes[i] = (52 + i - '0'.toInt()).toByte()
            i++
        }
        codes['+'.toInt()] = 62
        codes['/'.toInt()] = 63
    }
    //	    public static void main(String[] args) throws Exception {
    //	        // 加密成base64
    //	        String strSrc = "林";
    //	        String strOut = new String(Base64.encode(strSrc.getBytes("GB18030")));
    //	        System.out.println(strOut);
    //
    //	    }

}