package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ToffeeApi {


    @POST("re-registration")
    suspend fun signinByPhone(@Body signinByPhoneRequest: SigninByPhoneRequest): Response<SigninByPhoneResponse>
    @POST("confirm-code")
    suspend fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest):Response<VerifyCodeResponse>
    @POST("subscriber-profile")
    suspend fun getCustomerProfile(@Body profileRequest: ProfileRequest):Response<ProfileResponse>
    @POST("categories")
    suspend fun getCategory(@Body navCategoryRequest: NavCategoryRequest):Response<NavCategoryResponse>
    @POST("contents-v2")
    suspend fun getContents(@Body contentRequest: ContentRequest):Response<ContentResponse>
    @POST("feature-contents")
    suspend fun getFeatureContents(@Body featureContentRequest: FeatureContentRequest):Response<FeatureContentResponse>
    @POST("history-contents")
    suspend fun getHistoryContents(@Body historyContentRequest: HistoryContentRequest):Response<HistoryContentResponse>
    @POST("favorite-contents")
    suspend fun getFavoriteContents(@Body favoriteContentRequest: FavoriteContentRequest):Response<FavoriteContentResponse>
    @POST("app-home-page-content-tofee")
    suspend fun getChannels(@Body allChannelRequest: AllChannelRequest):Response<AllChannelResponse>
    @POST("relative-contents-ext")
    suspend fun getRelativeContents(@Body relativeContentRequest: RelativeContentRequest):Response<RelativeContentResponse>
    @POST("viewing-content")
    suspend fun sendViewingContent(@Body viewingContentRequest: ViewingContentRequest):Response<ViewingContentResponse>
    @POST("set-favorites")
    suspend fun updateFavorite(@Body favoriteRequest: FavoriteRequest):Response<FavoriteResponse>
    @POST("subscriber-profile-update")
    suspend fun updateProfile(@Body updateProfileRequest: UpdateProfileRequest):Response<UpdateProfileResponse>
    @POST("subscriber-profile-photo")
    suspend fun uploadPhoto(@Body updateProfilePhotoRequest: UploadProfileImageRequest):Response<UploadProfileImageResponse>
    @POST("search-contents")
    suspend fun searchContent(@Body searchContentRequest: SearchContentRequest):Response<SearchContentResponse>
    @POST("contents-shareable")
    suspend fun getContentFromShareableUrl(@Body shareableRequest: ContentShareableRequest):Response<ContentShareableResponse>
    @POST("heart-beat")
    suspend fun sendHeartBeat(@Body heartBeatRequest: HeartBeatRequest):Response<HeartBeatResponse>
    @POST("packages-with-subscription")
    suspend fun getPackageList(@Body packageListRequest: PackageListRequest):Response<PackageListResponse>
    @POST("package-details-v2")
    suspend fun getPackageChannelList(@Body packageChannelListRequest: PackageChannelListRequest):Response<PackageChannelListResponse>
    @POST("subscribe-a-package")
    suspend fun subscribePackage(@Body subscribePackageRequest: SubscribePackageRequest):Response<SubscribePackageResponse>
    @POST("set-auto-renew")
    suspend fun setAutoRenew(@Body autoRenewRequest: AutoRenewRequest):Response<AutoRenewResponse>
    @POST("set-fcm-token")
    suspend fun setFcmToken(@Body fcmTokenRequest: FcmTokenRequest):Response<FcmTokenResponse>
    @POST("my-referral-code")
    suspend fun getMyReferralCode(@Body referralCodeRequest: ReferralCodeRequest):Response<ReferralCodeResponse>
    @POST("referral-code-status")
    suspend fun checkReferralCode(@Body referralCodeStatusRequest: ReferralCodeStatusRequest):Response<ReferralCodeStatusResponse>
    @POST("redeem-referral-code")
    suspend fun  redeemReferralCode(@Body redeemReferralCodeRequest: RedeemReferralCodeRequest): Response<RedeemReferralCodeResponse>
    @POST("referrer-policy")
    suspend fun getReferrerPolicy(@Body referrerPolicyRequest: ReferrerPolicyRequest):Response<ReferrerPolicyResponse>

}