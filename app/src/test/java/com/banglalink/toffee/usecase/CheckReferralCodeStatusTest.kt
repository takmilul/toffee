package com.banglalink.toffee.usecase

import com.banglalink.toffee.Constants.INVALID_REFERRAL_ERROR_CODE
import com.banglalink.toffee.apiservice.CheckReferralCodeStatus
import com.banglalink.toffee.data.exception.ApiException
import com.banglalink.toffee.data.network.request.ReferralCodeStatusRequest
import com.banglalink.toffee.data.network.response.ReferralCodeStatusResponse
import com.banglalink.toffee.model.ReferralCodeStatusBean
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response

class CheckReferralCodeStatusTest : BaseUseCaseTest() {

    @Test(expected = ApiException::class)
    fun invalidReferralCodeTest() {
        runBlocking {
            try {
                whenever(mockToffeeApi.checkReferralCode(any<ReferralCodeStatusRequest>())).thenReturn(
                    Response.success(
                        ReferralCodeStatusResponse(
                            ReferralCodeStatusBean(
                                "Invalid",
                                "Your referral code is invalid"
                            )
                        )
                    ).body()
                )

                val result = CheckReferralCodeStatus(mockToffeeApi)
                result.execute("+88012345", "12345")

            } catch (e: ApiException) {
                assertEquals("Your referral code is invalid", e.errorMessage)
                assertEquals(INVALID_REFERRAL_ERROR_CODE, e.errorCode)
                throw e
            }

        }

    }
}