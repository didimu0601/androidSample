package kr.co.didimu.ulotto.util

enum class ServerType(val code: Int, private val apiUrl: String, private val webUrl: String) {
    REL(Constants.RELSERVER, "http://ulotto.didimu.co.kr", "http://ulotto.didimu.co.kr"),
    DEV(Constants.DEVSERVER, "http://ulotto.didimu.co.kr", "http://ulotto.didimu.co.kr"),
    QA(Constants.QASERVER, "http://ulotto.didimu.co.kr", "http://ulotto.didimu.co.kr"),
    SSLREL(Constants.SSLRELSERVER, "http://ulotto.didimu.co.kr", "http://ulotto.didimu.co.kr"),
    SSLDEV(Constants.SSLDEVSERVER, "http://ulotto.didimu.co.kr", "http://ulotto.didimu.co.kr"),
    SSLQA(Constants.SSLQASERVER, "http://ulotto.didimu.co.kr", "http://ulotto.didimu.co.kr");

    fun getServerCode(): Int {
        return this.code
    }

    fun getApiUrl(): String {
        return this.apiUrl
    }

    fun getWebUrl(): String {
        return this.webUrl
    }

    fun test() {
        for (type: ServerType in values()) {
        }
    }

    companion object {
        var apiUrl: String = REL.getApiUrl()
        var webUrl: String = REL.getWebUrl()

        fun from(findValue: Int?): ServerType {
            var type = REL
            findValue ?: return type

            try {
                type = values().first { it.code == findValue }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return type
        }
    }
}