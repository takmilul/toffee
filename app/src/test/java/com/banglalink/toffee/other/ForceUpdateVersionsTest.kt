package com.banglalink.toffee.other

import org.junit.Assert
import org.junit.Test

class ForceUpdateVersionsTest {
    fun shouldForceUpdate(versionCode: Int, forcedUpdateVersions: String? = null): Boolean {
        forcedUpdateVersions?.let {
            if(versionCode.toString() in it.split(",")) return true
        }
        return false
    }

    @Test
    fun testForceUpdateVersionCodes() {
        val forcedUpdateVersions: String = "45,46,47,48"
        Assert.assertEquals(false, shouldForceUpdate(45))
        Assert.assertEquals(false, shouldForceUpdate(44, forcedUpdateVersions))
        Assert.assertEquals(true, shouldForceUpdate(48, forcedUpdateVersions))
    }
}