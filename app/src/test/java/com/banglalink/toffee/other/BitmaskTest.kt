package com.banglalink.toffee.other

import org.junit.Assert.assertTrue
import org.junit.Test

class BitmaskTest {
    @Test
    fun testBitmask() {
        val mask = 0x7FFFFFFF
        assertTrue(mask and 1 == 1)
        assertTrue(mask and 2 == 2)
        assertTrue(mask and 3 == 3)
        assertTrue(mask and 4 == 4)
        assertTrue(mask and 8 == 8)
        assertTrue(mask and 16 == 16)
        assertTrue(mask and 32 == 32)
        assertTrue(mask and 64 == 64)

        val mask1 = 1 or 2 or 3
        assertTrue(mask1 and 1 == 1)
        assertTrue(mask1 and 3 == 3)
        assertTrue(mask1 and 4 != 4)

        val mask2 = 2 or 4 or 32
        assertTrue(mask2 and 2 == 2)
        assertTrue(mask2 and 1 != 1)
        assertTrue(mask2 and 32 == 32)
        assertTrue(mask2 and 16 != 16)
    }
}