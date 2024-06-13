package com.banglalink.toffee

import com.banglalink.toffee.lib.BuildConfig

object Constants {
    const val DEVICE_TYPE = BuildConfig.DEVICE_TYPE
    
    const val PREMIUM = 1
    const val NON_PREMIUM = 0
    const val PLAY_IN_WEB_VIEW = 1
    const val PLAY_CDN = 3
    const val STINGRAY_CONTENT = 10
    const val PLAY_IN_NATIVE_PLAYER = 0
    const val OPEN_IN_EXTERNAL_BROWSER = 2
    const val PLAYER_EVENT_TAG = "PLAYER_EVENT"
    const val IN_APP_UPDATE_REQUEST_CODE = 0x100
    
    const val DEVICE_ID_HEADER = "DEVICE-ID"
    const val OUTSIDE_OF_BD_ERROR_CODE = 403
    const val INVALID_REFERRAL_ERROR_CODE = -100
    const val MULTI_DEVICE_LOGIN_ERROR_CODE = 109
    const val ACCOUNT_DELETED_ERROR_CODE = 113
    const val UN_ETHICAL_ACTIVITIES_ERROR_CODE = 402
    const val CLIENT_API_HEADER = "CLIENT-API-HEADER"
    const val HE_SESSION_TOKEN_HEADER = "ENRICHMENT-SESSION-TOKEN"
    
    
    //    https://github.com/shamanland/simple-string-obfuscator
    val TOFFEE_KEY = object : Any() {
        var t = 0
        override fun toString(): String {
            val buf = ByteArray(16)
            t = -1930939990
            buf[0] = (t ushr 8).toByte()
            t = -1504968233
            buf[1] = (t ushr 21).toByte()
            t = 130652637
            buf[2] = (t ushr 11).toByte()
            t = 1497997422
            buf[3] = (t ushr 11).toByte()
            t = -425192637
            buf[4] = (t ushr 21).toByte()
            t = 1653453717
            buf[5] = (t ushr 14).toByte()
            t = 1701321722
            buf[6] = (t ushr 7).toByte()
            t = -924612210
            buf[7] = (t ushr 12).toByte()
            t = -670853495
            buf[8] = (t ushr 12).toByte()
            t = 51655855
            buf[9] = (t ushr 20).toByte()
            t = 1537623876
            buf[10] = (t ushr 13).toByte()
            t = -1556092827
            buf[11] = (t ushr 20).toByte()
            t = 1293143540
            buf[12] = (t ushr 22).toByte()
            t = -662934916
            buf[13] = (t ushr 9).toByte()
            t = -121737930
            buf[14] = (t ushr 9).toByte()
            t = -958150212
            buf[15] = (t ushr 3).toByte()
            return String(buf)
        }
    }.toString()
    
    val HE_KEY = object : Any() {
        var t = 0
        override fun toString(): String {
            val buf = ByteArray(16)
            t = 1491333727
            buf[0] = (t ushr 17).toByte()
            t = -1906451371
            buf[1] = (t ushr 10).toByte()
            t = 1708768173
            buf[2] = (t ushr 24).toByte()
            t = -1952310088
            buf[3] = (t ushr 6).toByte()
            t = 1442244974
            buf[4] = (t ushr 9).toByte()
            t = 1916285900
            buf[5] = (t ushr 3).toByte()
            t = -1914384722
            buf[6] = (t ushr 5).toByte()
            t = 276207619
            buf[7] = (t ushr 12).toByte()
            t = -1161896255
            buf[8] = (t ushr 9).toByte()
            t = 745598406
            buf[9] = (t ushr 16).toByte()
            t = 1187200402
            buf[10] = (t ushr 20).toByte()
            t = 2050354488
            buf[11] = (t ushr 15).toByte()
            t = -626910532
            buf[12] = (t ushr 6).toByte()
            t = -384464058
            buf[13] = (t ushr 3).toByte()
            t = -428062792
            buf[14] = (t ushr 20).toByte()
            t = 682937630
            buf[15] = (t ushr 9).toByte()
            return String(buf)
        }
    }.toString()
    
    const val GCP_CREDENTIAL = "eyJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsInByb2plY3RfaWQiOiAidG9mZmVlLTI2MTUwNyIsInByaXZhdGVfa2V5X2lkIjogImM3NzkzYzk4Y2RmZDFmZDUxZGY0NjQ5NmQ2YzZkZGU0ZTVhMWY4YWQiLCJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2QUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktZd2dnU2lBZ0VBQW9JQkFRQ2k2aTZ2VnBMQkpveWpcbmxSbno1bWlTNldFQjM4dEZ5RlplRHB6M3NUNWRaeVlLTCtoNU9HMzArazgyN3hpbHkrU3ZEREU3aFlCbnh3MnBcbnFodDRzNUxCSG5EUW4vTk81cVgvWXBCY1F6bklIamtoQ1NxY0poNnVlaU0rNFBkVmhnNXVGYzFGOTJQUkxJcHFcbmdQY1RRUVFJNmJ0OTBjSmRFSlZTVkRkUkp1M0htYzg4WTdrN3A2VDJENkp1U0NyakN2STFla3JkOWlROVE3K2tcblpPVTZkNjBPdTVhQW9hUzF2QTdkMEJ3RzE4RTN0cFJJY1pMY21oUk02TjdMRXZJZmxPby9reVNFQjBBVGlVUGFcblBvRDlXVGtTNjhwVll6aFMwL2djRUgvTzV0M1IxWWhCdHlNQmx0SXRnb1VtdFFLVGxpVFZrOVJubTJiZW5zVEpcblhHYkxEcy96QWdNQkFBRUNnZ0VBSlF5TXdXTTJ6S1l2cGFQcTJ3U3pFM1h2a2JXTFRMdGFxTms1NlRwdHJHQWdcbnpvQkdwZ3FuTnVjVnpMQmpNcEZEMkdzMVp3dmRQN3lNNXVEQWpwcUg3Vk0yNUxyN1loNGg4Z3BBT2pzdmtNc09cbnd1eUt2Q2dQSWk2MGhVQXovMGNWVVFPMFF3MFhyaUlxK0QyTDh0eitSZWloc3VOWkNFRVZORHZpdUpuWnlXSmNcbkdIbkZxRnlHYkw1bUFKd3NKaG55YkVhd1RpbWhHVGdNcTkrTnNoS1puaGlBVHB0dFhFdjF2SmZmN01nbW5tcUxcbnlZamhwbWpJZkgrelYvbFByNUl6VHk0Q0ZPaVRrQm9JeEpSb3NXRUhHR0R3aDNTdnJoWEZ2d2Q4Nk9WeGN1TkFcbkNXNjBpSXJic3RmNG9YYStBRnByNlcvR0xnYXIzbmJxZVNTZVlDblh3UUtCZ1FEYkpsR3JKMFVpaW1ET3V5YlhcbmlVMmZ2YXBkQjZCa2JRZ29FVlJTWWkvQ25BSHBrbWU1UkkyOEdna2pXZlI2NWx0bzJycytnWjlYWnY1ajJYTm5cbkJDa2E1SThVZHNFRTFycjM5d1B1Mkhtalg1STU4R0JEREZWclJvT0NzdmVoWVJkWnlRMVlDcm5SLzR5eWFHWlRcbks1R3lEZlRTVjFPN1ZqQ2FTQW5EbVBFWi9RS0JnUUMrVHlJcUV6OTljN1VaekJvWkw1WW1oc1ZvTUU2ZDB0THJcbmQxOUl3RjJ6ZUpqL3F4ckwxNGZocWg5QURNejBsUEYySEd2TURkbGRsSkI2UXlxY1orMTZhU1dSMjFrTWdXcUZcbkV6djk2ZVRNWGVEbWFINmsrb3lBSEJkTHM5OWtzbGh5SXFvYVE2Lytic0dMODM4emVieUNHbzRjNVpsQ3ZIWkJcbjNVUitBeDM4cndLQmdBNStGRHlMVmtrWWVacHlTZmxLL1RVcEo5RUxKaEkrRzZINnMyYlhVMjA5UCtzZ1lYZWhcbkR2WjNpazhxTGI3SzZSU2dFbTNweUkzUkk5V2FsY1VrUTB1aC9jeE9waWtad2plUHdGVFZRQVlMMWZHNjhjSzlcbldCREdFd0Jic1lDY21SeTQ3UWI3ZnBwaW1mMWFaaG50OGE2aXVhUUhYbVF2S1B1cTgrcFpmT2hKQW9HQUpFS2hcbnFOQ29EVGdwcm5Pa0dWNW14b3B4UEl2UHZ1UjNpYVBpQk5PYVJ1TFlqQVpIQWNYY1B4UVNCanNxSmdQbkNmRWFcblhQUGFrYWtINmpPMy83c1JsWHhKUzBRMjhGb01PTFRVZm1tOVRXOHB0ZHo5SEdRY3R5QWpGQ3AraXdvc0xmc0tcbjNCdy9nVVFMSHFaVGV6UmJ4WkpYdDR2THF0cUwwYnJlSFFCdEMwTUNnWUF6U2U1c2QwQi9BK05wNW5haGRmdFNcblFhM3NXSnpGVW9QNjZyajNtMG9EbG1STWZBTTRWa0xKcVc2R0tlK2RkMmxKbWdkdHhuK1VlYlhvdUlIeUJVMVRcbjBLWjE1R001Qzhic0lYNXhza2lLTXFHZStnWnNSb1crREJXdkM2eC9oUnZiclVKTnlKeEZTZ2hTdEF6L25NczlcbmZvN0NjL0FRM2wyQ2pWSytkN1piTFE9PVxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwiY2xpZW50X2VtYWlsIjogImZjbS1wdWItc3ViLXB1Ymxpc2hlckB0b2ZmZWUtMjYxNTA3LmlhbS5nc2VydmljZWFjY291bnQuY29tIiwiY2xpZW50X2lkIjogIjExNTg4NTgwNzc3NDY2MzI0ODg1NiIsImF1dGhfdXJpIjogImh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi9hdXRoIiwidG9rZW5fdXJpIjogImh0dHBzOi8vb2F1dGgyLmdvb2dsZWFwaXMuY29tL3Rva2VuIiwiYXV0aF9wcm92aWRlcl94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL29hdXRoMi92MS9jZXJ0cyIsImNsaWVudF94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL3JvYm90L3YxL21ldGFkYXRhL3g1MDkvZmNtLXB1Yi1zdWItcHVibGlzaGVyJTQwdG9mZmVlLTI2MTUwNy5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSJ9"
    
    val STAGING_URL = object : Any() {
        var t = 0
        override fun toString(): String {
            val buf = ByteArray(34)
            t = -63749156
            buf[0] = (t ushr 11).toByte()
            t = 1726582706
            buf[1] = (t ushr 17).toByte()
            t = -2100482327
            buf[2] = (t ushr 1).toByte()
            t = 278334318
            buf[3] = (t ushr 12).toByte()
            t = -852091656
            buf[4] = (t ushr 6).toByte()
            t = 1787786159
            buf[5] = (t ushr 4).toByte()
            t = 1565396159
            buf[6] = (t ushr 2).toByte()
            t = 1690204439
            buf[7] = (t ushr 18).toByte()
            t = 1833016675
            buf[8] = (t ushr 21).toByte()
            t = 1046702633
            buf[9] = (t ushr 17).toByte()
            t = -1827239342
            buf[10] = (t ushr 15).toByte()
            t = -744524966
            buf[11] = (t ushr 19).toByte()
            t = 336448422
            buf[12] = (t ushr 3).toByte()
            t = 1482102616
            buf[13] = (t ushr 22).toByte()
            t = -118335024
            buf[14] = (t ushr 6).toByte()
            t = 1832591914
            buf[15] = (t ushr 21).toByte()
            t = -992234948
            buf[16] = (t ushr 14).toByte()
            t = 631184296
            buf[17] = (t ushr 18).toByte()
            t = 1971704697
            buf[18] = (t ushr 13).toByte()
            t = -1434332882
            buf[19] = (t ushr 10).toByte()
            t = 1844743108
            buf[20] = (t ushr 21).toByte()
            t = -181613428
            buf[21] = (t ushr 9).toByte()
            t = 1824895937
            buf[22] = (t ushr 21).toByte()
            t = 1022778944
            buf[23] = (t ushr 12).toByte()
            t = -2101504813
            buf[24] = (t ushr 10).toByte()
            t = 453773542
            buf[25] = (t ushr 22).toByte()
            t = 953338796
            buf[26] = (t ushr 17).toByte()
            t = 570417627
            buf[27] = (t ushr 2).toByte()
            t = 1708280871
            buf[28] = (t ushr 24).toByte()
            t = -1761501295
            buf[29] = (t ushr 23).toByte()
            t = -1884930476
            buf[30] = (t ushr 12).toByte()
            t = 968807422
            buf[31] = (t ushr 18).toByte()
            t = 1303160020
            buf[32] = (t ushr 21).toByte()
            t = 49688024
            buf[33] = (t ushr 20).toByte()
            return String(buf)
        }
    }.toString()
    
    val PROD_URL = object : Any() {
        var t = 0
        override fun toString(): String {
            val buf = ByteArray(28)
            t = -1478385312
            buf[0] = (t ushr 10).toByte()
            t = -311521091
            buf[1] = (t ushr 13).toByte()
            t = 2087142437
            buf[2] = (t ushr 12).toByte()
            t = 882426592
            buf[3] = (t ushr 1).toByte()
            t = -1453791393
            buf[4] = (t ushr 9).toByte()
            t = 274967090
            buf[5] = (t ushr 12).toByte()
            t = 748126291
            buf[6] = (t ushr 15).toByte()
            t = 532773840
            buf[7] = (t ushr 11).toByte()
            t = 1869994842
            buf[8] = (t ushr 6).toByte()
            t = 876770836
            buf[9] = (t ushr 4).toByte()
            t = 2044470320
            buf[10] = (t ushr 14).toByte()
            t = -1050054710
            buf[11] = (t ushr 16).toByte()
            t = -877635573
            buf[12] = (t ushr 22).toByte()
            t = 2045439639
            buf[13] = (t ushr 5).toByte()
            t = -1215071505
            buf[14] = (t ushr 23).toByte()
            t = 1214669457
            buf[15] = (t ushr 16).toByte()
            t = 862516928
            buf[16] = (t ushr 23).toByte()
            t = 847838593
            buf[17] = (t ushr 23).toByte()
            t = -1811966262
            buf[18] = (t ushr 1).toByte()
            t = -1583051385
            buf[19] = (t ushr 5).toByte()
            t = 1356061915
            buf[20] = (t ushr 17).toByte()
            t = 1425939853
            buf[21] = (t ushr 6).toByte()
            t = 1718396551
            buf[22] = (t ushr 13).toByte()
            t = -1873495681
            buf[23] = (t ushr 10).toByte()
            t = -477076528
            buf[24] = (t ushr 8).toByte()
            t = -366659590
            buf[25] = (t ushr 7).toByte()
            t = -1288671765
            buf[26] = (t ushr 8).toByte()
            t = 1274755395
            buf[27] = (t ushr 22).toByte()
            return String(buf)
        }
    }.toString()
}