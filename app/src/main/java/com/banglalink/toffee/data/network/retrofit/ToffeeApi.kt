package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ToffeeApi {


    @POST("re-registration")
    suspend fun signinByPhone(@Body signinByPhoneRequest: SigninByPhoneRequest): SigninByPhoneResponse

    @POST("confirm-code")
    suspend fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest):VerifyCodeResponse

    @POST("subscriber-profile")
    suspend fun getCustomerProfile(@Body profileRequest: ProfileRequest):ProfileResponse

    @POST("categories-v2/1/{dbVersion}")//https://staging.toffee-cms.com/categories-v2/deviceType/dbVersion
    suspend fun getCategory(
        @Path("dbVersion") dbVersion: Int,
        @Body navCategoryRequest: NavCategoryRequest
    ):NavCategoryResponse

    @POST("contents-v5/1/{type}/1/{categoryId}/{subcategoryId}/{limit}/{offset}/{dbVersion}")//https://staging.toffee-cms.com/contents-v5/deviceType/type/telcoId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getContents(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body contentRequest: ContentRequest
    ): ContentResponse

    @POST("feature-contents")
    suspend fun getFeatureContents(@Body featureContentRequest: FeatureContentRequest):FeatureContentResponse

    @POST("feature-contents-v2/1/VOD/1/0/100/0/{dbVersion}")//https://staging.toffee-cms.com/feature-contents-v2/deviceType/type/telcoId/subCategoryId/li mit/offset/dbVersion
    suspend fun getFeatureContentsV2(
        @Path("dbVersion") dbVersion: Int,
        @Body featureContentRequest: FeatureContentRequest
    ):FeatureContentResponse

    @POST("history-contents")
    suspend fun getHistoryContents(@Body historyContentRequest: HistoryContentRequest):HistoryContentResponse

    @POST("favorite-contents")
    suspend fun getFavoriteContents(@Body favoriteContentRequest: FavoriteContentRequest):FavoriteContentResponse

    @POST("app-home-page-content-toffee-v2/1/0/1/200/{dbVersion}")//https://staging.toffee-cms.com/app-home-page-content-toffee-v2/deviceType/subCategoryId/telc oId/limit/dbVesion
    suspend fun getChannels(
        @Path("dbVersion") dbVersion: Int,
        @Body allChannelRequest: AllChannelRequest
    ):AllChannelResponse

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

    //*************** UGC APIS **********************//
    @POST("ugc-most-popular-contents/1/{type}/1/{dbVersion}")
    suspend fun getUgcMostPopularContents(
        @Path("type") type: String,
        @Path("dbVersion") dbVersion: Int,
        @Body mostPopularContentRequest: MostPopularContentRequest
    ): MostPopularContentResponse

    @POST("ugc-categories/1/{dbVersion}")
    suspend fun getUgcCategoryList(
        @Path("dbVersion") dbVersion: Int,
        @Body ugcCategoryRequest: UgcCategoryRequest
    ): UgcCategoryResponse

    @POST("ugc-category-wise-editors-choice/1/{type}/{isCategory}/{categoryId}/{dbVersion}")
    suspend fun getUgcEditorsChoice(
        @Path("type") type: String,
        @Path("isCategory") isCategory: Int = 0,
        @Path("categoryId") categoryId: Int = 0,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body ugcTrendingNowRequest: UgcTrendingNowRequest
    ): UgcTrendingNowResponse

    @POST("ugc-category-featured-contents/1/{type}/{isCategory}/{categoryId}/{dbVersion}")
    suspend fun getUgcFeatureContents(
        @Path("type") type: String,
        @Path("isCategory") isCategory: Int = 0,
        @Path("categoryId") categoryId: Int = 0,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body ugcFeatureRequest: UgcFeatureContentRequest
    ): UgcFeatureContentResponse

    @POST("ugc-popular-channel/1/{isCategory}/{categoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getUgcPopularChannels(
        @Path("isCategory") isCategory: Int = 0,
        @Path("categoryId") categoryId: Int = 0,
        @Path("limit") limit: Int = 0,
        @Path("offset") offset: Int = 0,
        @Path("dbVersion") dbVersion: Int,
        @Body ugcPopularChannelsRequest: UgcPopularChannelsRequest
    ): UgcPopularChannelsResponse

    @POST("ugc-content-upload")
    suspend fun uploadContent(
        @Body contentUploadRequest: ContentUploadRequest
    ): ContentUploadResponse

    @POST("ugc-popular-playlist-names/1/{dbVersion}")
    suspend fun getMostPopularPlaylists(
        @Path("dbVersion") dbVersion: Int,
        @Body mostPopularPlaylistsRequest: MostPopularPlaylistsRequest
    ): MostPopularPlaylistsResponse

    @POST("ugc-follow-on-category")
    suspend fun followOnCategory(@Body ugcFollowCategoryRequest: UgcFollowCategoryRequest): UgcFollowCategoryResponse

    @POST("ugc-subscribe-on-channel")
    suspend fun subscribeOnMyChannel(@Body myChannelSubscribeRequest: MyChannelSubscribeRequest): MyChannelSubscribeResponse

    @POST("ugc-delete-playlist-name")
    suspend fun deleteMyChannelPlaylist(@Body myChannelPlaylistDeleteRequest: MyChannelPlaylistDeleteRequest): MyChannelPlaylistDeleteResponse

    @POST("ugc-delete-content-to-playlist")
    suspend fun deleteMyChannelPlaylistVideo(@Body myChannelPlaylistVideoDeleteRequest: MyChannelPlaylistVideoDeleteRequest): MyChannelPlaylistVideoDeleteResponse

    @POST("ugc-add-content-to-playlist")
    suspend fun addToMyChannelPlayList(@Body myChannelAddToPlaylistRequest: MyChannelAddToPlaylistRequest): MyChannelAddToPlaylistResponse

    @POST("ugc-channel-details/1/{isOwner}/{channelId}/{dbVersion}")
    suspend fun getMyChannelDetails(
        @Path("isOwner") isOwner: Int = 0,
        @Path("channelId") channelId: Int = 2,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body channelDetailRequest: MyChannelDetailRequest
    ): MyChannelDetailResponse

    @POST("ugc-channel-all-content/1/{type}/{isOwner}/{channelId}/{categoryId}/{subcategoryId}/{limit}/{offset}/{dbVersion}")
    //ugc-channel-all-content/deviceType/type/isOwner/channelId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getMyChannelVideos(
        @Path("type") type: String,
        @Path("isOwner") isOwner: Int,
        @Path("channelId") channelId: Int,
        @Path("categoryId") categoryId: Int,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelVideosRequest: MyChannelVideosRequest
    ): MyChannelVideosResponse

    @POST("ugc-playlist-names/1/{isOwner}/{channelId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelPlaylist(
        @Path("isOwner") isOwner: Int,
        @Path("channelId") channelId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelPlaylistRequest: MyChannelPlaylistRequest
    ): MyChannelPlaylistResponse

    @POST("ugc-content-by-playlist/1/{channelId}/{isOwner}/{playlistId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelPlaylistVideos(
        @Path("channelId") channelId: Int,
        @Path("isOwner") isOwner: Int,
        @Path("playlistId") playlistId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelPlaylistVideosRequest: MyChannelPlaylistVideosRequest
    ): MyChannelPlaylistVideosResponse

    @POST("/ugc-edit-playlist-name")
    suspend fun editMyChannelPlaylist(@Body channelPlaylistEditRequest: MyChannelPlaylistEditRequest): MyChannelPlaylistEditResponse

    @POST("/ugc-create-playlist-name")
    suspend fun createMyChannelPlaylist(@Body createPlaylistRequest: MyChannelPlaylistCreateRequest): MyChannelPlaylistCreateResponse

    @POST("/ugc-channel-edit")
    suspend fun editMyChannelDetail(@Body createPlaylistEditRequest: MyChannelEditRequest): MyChannelEditResponse

    @POST("/ugc-rating-on-channel")
    suspend fun rateMyChannel(@Body myChannelRatingRequest: MyChannelRatingRequest): MyChannelRatingResponse
}