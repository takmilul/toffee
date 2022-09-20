package com.banglalink.toffee.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CustomCookieJar: CookieJar {
    
    private val cookieStore: HashMap<String, List<Cookie>> = HashMap()
    
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach {
            try {
                val domainString = it.value.substringAfter("Domain=").substringBefore("|").trim() //upper loop cookie domain part
                val isSecure = it.value.contains("Secure")
                val isHttpOnly = it.value.contains("HttpOnly")
                cookieStore[domainString] = cookies.map { cookie ->
                    val cookie = Cookie.Builder().apply {
                        name(cookie.name)
                        value(cookie.value)
                        expiresAt(cookie.expiresAt)
                        val currentDomainString = cookie.value.substringAfter("Domain=").substringBefore("|").trim() //individual cookie domain part
                        domain(currentDomainString)
                        path(cookie.path)
                        hostOnlyDomain(currentDomainString)
                        if (isSecure) secure()
                        if (isHttpOnly) httpOnly()
                    }.build()
                    cookie
                }.filter { it.domain == domainString } // filter only matching domain part with each domain
            } catch (e: Exception) {
                
            }
        }
    }
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return try {
            val urlString = url.toString()
            var cookies = cookieStore[urlString]
            cookies ?: run {
                cookieStore.keys.forEach {
                    if (urlString.contains(it)) {
                        cookies = cookieStore[it]
                    }
                }
            }
            return cookies ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }
}