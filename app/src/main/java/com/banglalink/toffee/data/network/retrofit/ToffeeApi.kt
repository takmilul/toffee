package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ToffeeApi {
    
//    @POST("re-registration")
    @POST("re-registration-v2")
    suspend fun loginByPhone(@Body signInByPhoneRequest: LoginByPhoneRequest): LoginByPhoneResponse

//    @POST("confirm-code")
    @POST("confirm-code-v2")
    suspend fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest):VerifyCodeResponse

    @POST("subscriber-profile")
    suspend fun getCustomerProfile(@Body profileRequest: ProfileRequest):ProfileResponse

    @POST("categories-v2/1/{dbVersion}")//https://staging.toffee-cms.com/categories-v2/deviceType/dbVersion
    suspend fun getCategory(
        @Path("dbVersion") dbVersion: Int,
        @Body navCategoryRequest: NavCategoryRequest
    ):NavCategoryResponse

    @POST("ugc-contents-v5/1/{type}/1/{categoryId}/{subcategoryId}/{limit}/{offset}/{dbVersion}")//https://staging.toffee-cms.com/contents-v5/deviceType/type/telcoId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getContents(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body contentRequest: ContentRequest
    ): ContentResponse

    @POST("history-contents")
    suspend fun getHistoryContents(@Body historyContentRequest: HistoryContentRequest):HistoryContentResponse

    @POST("ugc-favorite-contents")
    suspend fun getFavoriteContents(@Body favoriteContentRequest: FavoriteContentRequest):FavoriteContentResponse

    @POST("ugc-app-home-page-content-toffee-v2/1/0/1/200/{dbVersion}")//https://staging.toffee-cms.com/app-home-page-content-toffee-v2/deviceType/subCategoryId/telc oId/limit/dbVesion
    suspend fun getChannels(
        @Path("dbVersion") dbVersion: Int,
        @Body allChannelRequest: AllChannelRequest
    ):AllChannelResponse

    @POST("ugc-relative-contents-ext")
    suspend fun getRelativeContents(@Body relativeContentRequest: RelativeContentRequest):RelativeContentResponse

    @POST("viewing-content")
    suspend fun sendViewingContent(@Body viewingContentRequest: ViewingContentRequest):ViewingContentResponse

    @POST("set-ugc-favorites")
    suspend fun updateFavorite(@Body favoriteRequest: FavoriteRequest):FavoriteResponse

    @POST("subscriber-profile-update")
    suspend fun updateProfile(@Body updateProfileRequest: UpdateProfileRequest):UpdateProfileResponse

    @POST("subscriber-profile-photo")
    suspend fun uploadPhoto(@Body updateProfilePhotoRequest: UploadProfileImageRequest):UploadProfileImageResponse

    @POST("ugc-search-contents")
    suspend fun searchContent(@Body searchContentRequest: SearchContentRequest):SearchContentResponse

    @POST("contents-shareable")
    suspend fun getContentFromShareableUrl(@Body shareableRequest: ContentShareableRequest):ContentShareableResponse

    @POST("content-share-log")
    suspend fun sendShareLog(@Body shareableRequest: ContentShareLogRequest):ContentShareLogResponse

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
    @POST("ugc-most-popular-contents/1/{type}/{isSerialContent}/{categoryId}/{subCategoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getUgcMostPopularContents(
        @Path("type") type: String,
        @Path("isSerialContent") isDramaSeries: Int = 0,
        @Path("categoryId") categoryId: Int = 0,
        @Path("subCategoryId") subCategoryId: Int = 0,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body mostPopularContentRequest: MostPopularContentRequest
    ): MostPopularContentResponse

    @POST("ugc-categories/1/{dbVersion}")
    suspend fun getUgcCategoryList(
        @Path("dbVersion") dbVersion: Int,
        @Body categoryRequest: CategoryRequest
    ): CategoryResponse
    
    @POST("ugc-active-inactive-categories/1/{dbVersion}")
    suspend fun getUgcContentCategoryList(
        @Path("dbVersion") dbVersion: Int,
        @Body categoryRequest: ContentCategoryRequest
    ): CategoryResponse

    @POST("ugc-category-wise-editors-choice/1/{type}/{editorChoiceType}/{categoryId}/{dbVersion}")
    suspend fun getUgcEditorsChoice(
        @Path("type") type: String,
        @Path("editorChoiceType") isCategory: Int = 1,
        @Path("categoryId") categoryId: Int = 0,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body allUserChannelsEditorsChoiceRequest: AllUserChannelsEditorsChoiceRequest
    ): AllUserChannelsEditorsChoiceResponse

    @POST("ugc-category-featured-contents/1/{type}/{featureType}/{categoryId}/{dbVersion}")
    suspend fun getUgcFeatureContents(
        @Path("type") type: String,
        @Path("featureType") featureType: Int = 1,
        @Path("categoryId") categoryId: Int = 0,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body featureRequest: FeatureContentRequest
    ): FeatureContentResponse

    @POST("ugc-popular-channel/1/{isCategory}/{categoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getUgcPopularChannels(
        @Path("isCategory") isCategory: Int = 0,
        @Path("categoryId") categoryId: Int = 0,
        @Path("limit") limit: Int = 0,
        @Path("offset") offset: Int = 0,
        @Path("dbVersion") dbVersion: Int,
        @Body popularChannelsRequest: PopularChannelsRequest
    ): PopularChannelsResponse

    @POST("ugc-content-upload")
    suspend fun uploadContent(
        @Body contentUploadRequest: ContentUploadRequest
    ): ContentUploadResponse

    @POST("ugc-content-update")
    suspend fun editContent(
        @Body contentEditRequest: ContentEditRequest
    ): ContentEditResponse

    @POST("ugc-content-upload-confirmation")
    suspend fun uploadConfirmation(
        @Body uploadConfirmationRequest: UploadConfirmationRequest
    ): UploadConfirmationResponse

    @POST("ugc-popular-playlist-names/1/{dbVersion}")
    suspend fun getMostPopularPlaylists(
        @Path("dbVersion") dbVersion: Int,
        @Body mostPopularPlaylistsRequest: MostPopularPlaylistsRequest
    ): MostPopularPlaylistsResponse

    @POST("ugc-follow-on-category")
    suspend fun followOnCategory(@Body followCategoryRequest: FollowCategoryRequest): FollowCategoryResponse

    @POST("ugc-subscribe-on-channel")
    suspend fun subscribeOnMyChannel(@Body myChannelSubscribeRequest: MyChannelSubscribeRequest): MyChannelSubscribeResponse

    @POST("ugc-delete-playlist-name")
    suspend fun deleteMyChannelPlaylist(@Body myChannelPlaylistDeleteRequest: MyChannelPlaylistDeleteRequest): MyChannelPlaylistDeleteResponse

    @POST("ugc-delete-content-to-playlist")
    suspend fun deleteMyChannelPlaylistVideo(@Body myChannelPlaylistVideoDeleteRequest: MyChannelPlaylistVideoDeleteRequest): MyChannelPlaylistVideoDeleteResponse

    @POST("ugc-add-content-to-playlist")
    suspend fun addToMyChannelPlayList(@Body myChannelAddToPlaylistRequest: MyChannelAddToPlaylistRequest): MyChannelAddToPlaylistResponse

    @POST("ugc-channel-details/1/{channelOwnerId}/{isOwner}/{isPublic}/{channelId}/{dbVersion}")
    suspend fun getMyChannelDetails(
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("isOwner") isOwner: Int,
        @Path("isPublic") isPublic: Int,
        @Path("channelId") channelId: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelDetailRequest: MyChannelDetailRequest
    ): MyChannelDetailResponse

    @POST("ugc-channel-all-content/1/{type}/{isOwner}/{channelOwnerId}/{categoryId}/{subcategoryId}/{isPublic}/{limit}/{offset}/{dbVersion}")
    //ugc-channel-all-content/deviceType/type/isOwner/channelId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getMyChannelVideos(
        @Path("type") type: String,
        @Path("isOwner") isOwner: Int,
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("categoryId") categoryId: Int,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("isPublic") isPublic: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelVideosRequest: MyChannelVideosRequest
    ): MyChannelVideosResponse

    @POST("ugc-content-delete")
    suspend fun deleteMyChannelVideo(@Body myChannelVideoDeleteRequest: MyChannelVideoDeleteRequest): MyChannelVideoDeleteResponse

    @POST("ugc-playlist-names/1/{isOwner}/{channelOwnerId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelPlaylist(
        @Path("isOwner") isOwner: Int,
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelPlaylistRequest: MyChannelPlaylistRequest
    ): MyChannelPlaylistResponse

    @POST("ugc-content-by-playlist/1/{channelOwnerId}/{isOwner}/{playlistId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelPlaylistVideos(
        @Path("channelOwnerId") channelOwnerId: Int,
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

    @POST("ugc-terms-and-conditions/1/{dbVersion}")
    suspend fun getVideoTermsAndCondition(
        @Path("dbVersion") dbVersion: Int,
        @Body termsConditionRequest: TermsConditionRequest
    ): TermsAndConditionResponse
    
    @POST("/ugc-rating-on-channel")
    suspend fun rateMyChannel(@Body myChannelRatingRequest: MyChannelRatingRequest): MyChannelRatingResponse
    
    @POST("/ugc-all-user-channel/1/{limit}/{offset}/{dbVersion}")
    suspend fun getAllUserChannels(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body allUserChannelsRequest: AllUserChannelsRequest
    ): AllUserChannelsResponse
    
    @POST("/ugc-channel-subscription-list/1/{limit}/{offset}/{dbVersion}")
    suspend fun getSubscribedUserChannels(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body subscribedUserChannelsRequest: SubscribedUserChannelsRequest
    ): SubscribedUserChannelsResponse
    
    @POST("/ugc-movie-category-details/1/{type}/{categoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMovieCategoryDetail(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body categoryDetailRequest: MovieCategoryDetailRequest
    ): MovieCategoryDetailResponse
    
    @POST("/ugc-movie-preview/1/{type}/{categoryId}/{subCategoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMoviePreviews(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subCategoryId") subCategoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body moviesPreviewRequest: MoviesPreviewRequest
    ): MoviesPreviewResponse
    
    @POST("/ugc-coming-soon/1/{type}/{categoryId}/{subCategoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getComingSoonPosters(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subCategoryId") subCategoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body moviesComingSoonRequest: MoviesComingSoonRequest
    ): MoviesComingSoonResponse
    
    @POST("/ugc-latest-drama-serial/1/{type}/{subCategoryId}/{isFilter}/{hashTags}/{limit}/{offset}/{dbVersion}")
    suspend fun getDramaSeriesContents(
        @Path("type") type: String,
        @Path("subCategoryId") subCategoryId: Int,
        @Path("isFilter") isFilter: Int,
        @Path("hashTags") hashTag: String?,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body dramaSeriesContentRequest: DramaSeriesContentRequest
    ): DramaSeriesContentResponse

    @POST("/ugc-drama-serial-by-season/1/{type}/{serialSummaryId}/{seasonNo}/{limit}/{offset}/{dbVersion}")
    suspend fun getDramaEpisodsBySeason(
        @Path("type") type: String,
        @Path("serialSummaryId") serialSummaryId: Int,
        @Path("seasonNo") seasonNo: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body dramaEpisodesBySeasonRequest: DramaEpisodesBySeasonRequest
    ): DramaEpisodesBySeasonResponse
    
    @POST("/ugc-partner-list/1/{type}/{limit}/{offset}/{dbVersion}")
    suspend fun getPartnersList(
        @Path("type") type: String,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body partnersRequest: PartnersRequest
    ): PartnersResponse
    
    @POST("/mqtt-credential")
    suspend fun getMqttCredential(@Body mqttRequest: MqttRequest): MqttResponse
    
    @POST("/ugc-inappropriate-head-list/1/{limit}/{offset}/{dbVersion}")
    suspend fun getOffenseList(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body offenseRequest: OffenseRequest
    ): OffenseResponse
    
    @POST("/ugc-user-unverified")
    suspend fun unVerifyUser(
        @Body logoutRequest: LogoutRequest
    ): LogoutResponse
}