package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.*
import com.banglalink.toffee.model.FeaturedPartnerRequest
import com.banglalink.toffee.model.FeaturedPartnerResponse
import retrofit2.http.*

interface ToffeeApi {
    
//    @POST("re-registration")
    @POST("re-registration-v2")
    suspend fun loginByPhone(@Body signInByPhoneRequest: LoginByPhoneRequest): LoginByPhoneResponse

//    @POST("confirm-code")
    @POST("confirm-code-v2")
    suspend fun verifyCode(@Body verifyCodeRequest: VerifyCodeRequest):VerifyCodeResponse

    @POST("subscriber-profile")
    suspend fun getCustomerProfile(@Body profileRequest: ProfileRequest):ProfileResponse

    @POST("categories-v2/${Constants.DEVICE_TYPE}/{dbVersion}")//https://staging.toffee-cms.com/categories-v2/deviceType/dbVersion
    suspend fun getCategory(
        @Path("dbVersion") dbVersion: Int,
        @Body navCategoryRequest: NavCategoryRequest
    ):NavCategoryResponse

    @POST("ugc-contents-v5/${Constants.DEVICE_TYPE}/{type}/1/{categoryId}/{subcategoryId}/{limit}/{offset}/{dbVersion}")//https://staging.toffee-cms.com/contents-v5/deviceType/type/telcoId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getContents(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body contentRequest: ContentRequest
    ): ContentResponse

    @POST("stingray-contents/${Constants.DEVICE_TYPE}/{type}/1/{subcategoryId}/{limit}/{offset}/{dbVersion}")//https://staging.toffee-cms.com/contents-v5/deviceType/type/telcoId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getStingrayContents(
        @Path("type") type: String,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body contentRequest: StingrayConetntRequest
    ): ContentResponse

    @POST("ugc-fireworks-list/${Constants.DEVICE_TYPE}/NULL/NULL/10/0/{dbVersion}")  //{BaseUrl}/ugc-fireworks-list/deviceType/channel_id/playlist_id/limit/offset/dbVersion
    suspend fun getFireworks(
        @Path("dbVersion") dbVersion: Int,
        @Body fireworkRequest: FireworkRequest
    ): FireworkResponse

    @POST("history-contents")
    suspend fun getHistoryContents(@Body historyContentRequest: HistoryContentRequest):HistoryContentResponse

    @POST("ugc-favorite-contents")
    suspend fun getFavoriteContents(@Body favoriteContentRequest: FavoriteContentRequest):FavoriteContentResponse

    @POST("ugc-app-home-page-content-toffee-v2/${Constants.DEVICE_TYPE}/0/1/200/{dbVersion}")//https://staging.toffee-cms.com/app-home-page-content-toffee-v2/deviceType/subCategoryId/telc oId/limit/dbVesion
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
    @POST("ugc-most-popular-contents/${Constants.DEVICE_TYPE}/{type}/{isSerialContent}/{categoryId}/{subCategoryId}/{limit}/{offset}/{dbVersion}")
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

    @POST("ugc-categories/${Constants.DEVICE_TYPE}/{dbVersion}")
    suspend fun getUgcCategoryList(
        @Path("dbVersion") dbVersion: Int,
        @Body categoryRequest: CategoryRequest
    ): CategoryResponse

    @POST("ugc-payment-method-list/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
    suspend fun getPaymentMethodList(
        @Path("limit") limit:Int,
        @Path("offset") offset:Int,
        @Path("dbVersion") dbVersion: Int,
        @Body paymentMethodRequest: PaymentMethodRequest
    ): PaymentMethodResponse
    
    @POST("ugc-active-inactive-categories/${Constants.DEVICE_TYPE}/{dbVersion}")
    suspend fun getUgcContentCategoryList(
        @Path("dbVersion") dbVersion: Int,
        @Body categoryRequest: ContentCategoryRequest
    ): CategoryResponse

    @POST("ugc-category-wise-editors-choice/${Constants.DEVICE_TYPE}/{type}/{editorChoiceType}/{categoryId}/{dbVersion}")
    suspend fun getUgcEditorsChoice(
        @Path("type") type: String,
        @Path("editorChoiceType") isCategory: Int = 1,
        @Path("categoryId") categoryId: Int = 0,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body allUserChannelsEditorsChoiceRequest: AllUserChannelsEditorsChoiceRequest
    ): AllUserChannelsEditorsChoiceResponse

    @POST("ugc-category-featured-contents/${Constants.DEVICE_TYPE}/{type}/{featureType}/{categoryId}/{dbVersion}")
    suspend fun getUgcFeatureContents(
        @Path("type") type: String,
        @Path("featureType") featureType: Int = 1,
        @Path("categoryId") categoryId: Int = 0,
        @Path("dbVersion") dbVersion: Int = 0,
        @Body featureRequest: FeatureContentRequest
    ): FeatureContentResponse

    @POST("ugc-popular-channel/${Constants.DEVICE_TYPE}/{isCategory}/{categoryId}/{limit}/{offset}/{dbVersion}")
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

    @POST("ugc-popular-playlist-names/${Constants.DEVICE_TYPE}/{dbVersion}")
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

    @POST("ugc-channel-details/${Constants.DEVICE_TYPE}/{channelOwnerId}/{isOwner}/{isPublic}/{channelId}/{dbVersion}")
    suspend fun getMyChannelDetails(
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("isOwner") isOwner: Int,
        @Path("isPublic") isPublic: Int,
        @Path("channelId") channelId: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelDetailRequest: MyChannelDetailRequest
    ): MyChannelDetailResponse

    @POST("ugc-channel-all-content/${Constants.DEVICE_TYPE}/{type}/{isOwner}/{channelOwnerId}/{categoryId}/{subcategoryId}/{isPublic}/{limit}/{offset}/{dbVersion}")
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

    @POST("ugc-playlist-names/${Constants.DEVICE_TYPE}/{isOwner}/{channelOwnerId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelPlaylist(
        @Path("isOwner") isOwner: Int,
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelPlaylistRequest: MyChannelPlaylistRequest
    ): MyChannelPlaylistResponse

    @POST("ugc-user-playlist-names/${Constants.DEVICE_TYPE}/{isOwner}/{channelOwnerId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelUserPlaylist(
        @Path("isOwner") isOwner: Int,
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelUserPlaylistRequest: MyChannelUserPlaylistRequest
    ): MyChannelPlaylistResponse

    @POST("ugc-content-by-playlist/${Constants.DEVICE_TYPE}/{channelOwnerId}/{isOwner}/{playlistId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelPlaylistVideos(
        @Path("channelOwnerId") channelOwnerId: Int,
        @Path("isOwner") isOwner: Int,
        @Path("playlistId") playlistId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelPlaylistVideosRequest: MyChannelPlaylistVideosRequest
    ): MyChannelPlaylistVideosResponse

    @POST("ugc-content-by-user-playlist/${Constants.DEVICE_TYPE}/{channelOwnerUserId}/{isOwner}/{playlistId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMyChannelUserPlaylistVideos(
        @Path("channelOwnerUserId") channelOwnerId: Int,
        @Path("isOwner") isOwner: Int,
        @Path("playlistId") playlistId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body channelUserPlaylistVideosRequest: MyChannelUserPlaylistVideosRequest
    ): MyChannelPlaylistVideosResponse

    @POST("/ugc-edit-playlist-name")
    suspend fun editMyChannelPlaylist(@Body channelPlaylistEditRequest: MyChannelPlaylistEditRequest): MyChannelPlaylistEditResponse

    @POST("/ugc-create-playlist-name")
    suspend fun createMyChannelPlaylist(@Body createPlaylistRequest: MyChannelPlaylistCreateRequest): MyChannelPlaylistCreateResponse

    @POST("/ugc-channel-edit")
    suspend fun editMyChannelDetail(@Body createPlaylistEditRequest: MyChannelEditRequest): MyChannelEditResponse

    @POST("ugc-terms-and-conditions/${Constants.DEVICE_TYPE}/{dbVersion}")
    suspend fun getVideoTermsAndCondition(
        @Path("dbVersion") dbVersion: Int,
        @Body termsConditionRequest: TermsConditionRequest
    ): TermsAndConditionResponse
    
    @POST("/ugc-rating-on-channel")
    suspend fun rateMyChannel(@Body myChannelRatingRequest: MyChannelRatingRequest): MyChannelRatingResponse
    
    @POST("/ugc-all-user-channel/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
    suspend fun getAllUserChannels(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body allUserChannelsRequest: AllUserChannelsRequest
    ): AllUserChannelsResponse
    
    @POST("/ugc-channel-subscription-list/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
    suspend fun getSubscribedUserChannels(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body subscribedUserChannelsRequest: SubscribedUserChannelsRequest
    ): SubscribedUserChannelsResponse
    
    @POST("/ugc-movie-category-details/${Constants.DEVICE_TYPE}/{type}/{categoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMovieCategoryDetail(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body categoryDetailRequest: MovieCategoryDetailRequest
    ): MovieCategoryDetailResponse
    
    @POST("/ugc-movie-preview/${Constants.DEVICE_TYPE}/{type}/{categoryId}/{subCategoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getMoviePreviews(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subCategoryId") subCategoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body moviesPreviewRequest: MoviesPreviewRequest
    ): MoviesPreviewResponse
    
    @POST("/ugc-coming-soon/${Constants.DEVICE_TYPE}/{type}/{categoryId}/{subCategoryId}/{limit}/{offset}/{dbVersion}")
    suspend fun getComingSoonPosters(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subCategoryId") subCategoryId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body moviesComingSoonRequest: MoviesComingSoonRequest
    ): MoviesComingSoonResponse
    
    @POST("/ugc-latest-drama-serial/${Constants.DEVICE_TYPE}/{type}/{subCategoryId}/{isFilter}/{hashTags}/{limit}/{offset}/{dbVersion}")
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

    @POST("/ugc-drama-serial-by-season/${Constants.DEVICE_TYPE}/{type}/{serialSummaryId}/{seasonNo}/{limit}/{offset}/{dbVersion}")
    suspend fun getDramaEpisodsBySeason(
        @Path("type") type: String,
        @Path("serialSummaryId") serialSummaryId: Int,
        @Path("seasonNo") seasonNo: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body dramaEpisodesBySeasonRequest: DramaEpisodesBySeasonRequest
    ): DramaEpisodesBySeasonResponse
    
    @POST("/ugc-partner-list/${Constants.DEVICE_TYPE}/{type}/{limit}/{offset}/{dbVersion}")
    suspend fun getPartnersList(
        @Path("type") type: String,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body partnersRequest: PartnersRequest
    ): PartnersResponse
    
    @POST("/mqtt-credential")
    suspend fun getMqttCredential(@Body mqttRequest: MqttRequest): MqttResponse
    
    @POST("/ugc-inappropriate-head-list/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
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

    @POST("vast-tags-list/${Constants.DEVICE_TYPE}/{dbVersion}")
    suspend fun getVastTagLists(
        @Path("dbVersion") dbVersion: Int,
        @Body paymentMethodRequest: VastTagRequest
    ): VastTagResponse

    @POST
    suspend fun getDrmToken(
        @Url url: String,
        @Header("DRM-API-HEADER") drmHeader: String,
        @Body drmTokenRequest: DrmTokenRequest
    ): DrmTokenResponse
    
    @POST
    suspend fun getHeaderEnrichment(
        @HeaderMap headerMap: Map<String, String>,
        @Url url: String? = "http://bl-he.toffeelive.com/getMsisdn.php",
    ): HeaderEnrichmentResponse
    
    @POST("/create-upload-signed-url")
    suspend fun uploadSignedUrl(
        @Body uploadSignedUrlRequest: UploadSignedUrlRequest
    ): UploadSignedUrlResponse

    @POST("/ugc-feature-partner-list/${Constants.DEVICE_TYPE}/{type}/{limit}/{offset}/{dbVersion}")
    suspend fun getFeaturedPartners(
        @Path("type") type: String,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body featuredPartnerRequest: FeaturedPartnerRequest
    ): FeaturedPartnerResponse

}