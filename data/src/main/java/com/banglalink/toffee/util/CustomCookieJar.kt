package com.banglalink.toffee.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CustomCookieJar: CookieJar {
    
    private val cookieStore: HashMap<String, List<Cookie>> = HashMap()
    
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach {
            try {
                val domain = it.value.substringAfter("Domain=").substringBefore("|").trim()
                cookieStore[domain] = cookies
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