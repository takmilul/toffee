package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.*
import com.banglalink.toffee.model.CLIENT_API_HEADER
import retrofit2.Response
import retrofit2.http.*

interface ToffeeApi {


    @POST("re-registration")
    suspend fun signinByPhone(@Body signinByPhoneRequest: SigninByPhoneRequest): SigninByPhoneResponse
    @POST("confirm-code")
    suspend fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest):VerifyCodeResponse
    @POST("subscriber-profile")
    suspend fun getCustomerProfile(@Body profileRequest: ProfileRequest):ProfileResponse
    @POST("categories-v2/1/{dbVersion}")//https://staging.toffee-cms.com/categories-v2/deviceType/dbVersion
    suspend fun getCategory(@Path("dbVersion") dbVersion:Int,@Body navCategoryRequest: NavCategoryRequest):NavCategoryResponse
    @POST("contents-v5/1/{type}/1/{categoryId}/0/30/{offset}/{dbVersion}")//https://staging.toffee-cms.com/contents-v5/deviceType/type/telcoId/categoryId/subCategoryI d/limit/offset/dbVersion
    suspend fun getContents(@Path("categoryId") categoryId:Int,@Path("offset") offset:Int,@Path("type") type:String, @Path("dbVersion") dbVersion:Int,@Body contentRequest: ContentRequest):ContentResponse
    @POST("feature-contents")
    suspend fun getFeatureContents(@Body featureContentRequest: FeatureContentRequest):FeatureContentResponse
    @POST("feature-contents-v2/1/VOD/1/0/100/0/{dbVersion}")//https://staging.toffee-cms.com/feature-contents-v2/deviceType/type/telcoId/subCategoryId/li mit/offset/dbVersion
    suspend fun getFeatureContentsV2(@Path("dbVersion") dbVersion:Int,@Body featureContentRequest: FeatureContentRequest):FeatureContentResponse
    @POST("history-contents")
    suspend fun getHistoryContents(@Body historyContentRequest: HistoryContentRequest):HistoryContentResponse
    @POST("favorite-contents")
    suspend fun getFavoriteContents(@Body favoriteContentRequest: FavoriteContentRequest):FavoriteContentResponse
    @POST("app-home-page-content-toffee-v2/1/0/1/200/{dbVersion}")//https://staging.toffee-cms.com/app-home-page-content-toffee-v2/deviceType/subCategoryId/telc oId/limit/dbVesion
    suspend fun getChannels(@Path("dbVersion") dbVersion:Int,@Body allChannelRequest: AllChannelRequest):AllChannelResponse
    @POST("relative-contents-ext")
    suspend fun getRelativeContents(@Body relativeContentRequest: RelativeContentRequest):RelativeContentResponse
    @POST("viewing-content")
    suspend fun sendViewingContent(@Body viewingContentRequest: ViewingContentRequest):ViewingContentResponse
    @POST("set-favorites")
    suspend fun updateFavorite(@Body favoriteRequest: FavoriteRequest):FavoriteResponse
    @POST("subscriber-profile-update")
    suspend fun updateProfile(@Body updateProfileRequest: UpdateProfileRequest):UpdateProfileResponse
    @POST("subscriber-profile-photo")
    suspend fun uploadPhoto(@Body updateProfilePhotoRequest: UploadProfileImageRequest):UploadProfileImageResponse
    @POST("search-contents")
    suspend fun searchContent(@Body searchContentRequest: SearchContentRequest):SearchContentResponse
    @POST("contents-shareable")
    suspend fun getContentFromShareableUrl(@Body shareableRequest: ContentShareableRequest):ContentShareableResponse
    @POST("heart-beat")
    suspend fun sendHeartBeat(@Body heartBeatRequest: HeartBeatRequest):HeartBeatResponse
    @POST("packages-with-subscription")
    suspend fun getPackageList(@Body packageListRequest: PackageListRequest):PackageListResponse
    @POST("package-details-v2")
    suspend fun getPackageChannelList(@Body packageChannelListRequest: PackageChannelListRequest):PackageChannelListResponse
    @POST("subscribe-a-package")
    suspend fun subscribePackage(@Body subscribePackageRequest: SubscribePackageRequest):SubscribePackageResponse
    @POST("set-auto-renew")
    suspend fun setAutoRenew(@Body autoRenewRequest: AutoRenewRequest):AutoRenewResponse
    @POST("set-fcm-token")
    suspend fun setFcmToken(@Body fcmTokenRequest: FcmTokenRequest):FcmTokenResponse
    @POST("my-referral-code")
    suspend fun getMyReferralCode(@Body referralCodeRequest: ReferralCodeRequest):ReferralCodeResponse
    @POST("referral-code-status")
    suspend fun checkReferralCode(@Body referralCodeStatusRequest: ReferralCodeStatusRequest):ReferralCodeStatusResponse
    @POST("redeem-referral-code")
    suspend fun  redeemReferralCode(@Body redeemReferralCodeRequest: RedeemReferralCodeRequest): RedeemReferralCodeResponse
    @POST("referrer-policy")
    suspend fun getReferrerPolicy(@Body referrerPolicyRequest: ReferrerPolicyRequest):ReferrerPolicyResponse

}