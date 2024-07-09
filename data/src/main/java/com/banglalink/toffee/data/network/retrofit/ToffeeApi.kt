package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.network.request.AccountDeleteRequest
import com.banglalink.toffee.data.network.request.AddTokenizedAccountInitRequest
import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.request.AllUserChannelsEditorsChoiceRequest
import com.banglalink.toffee.data.network.request.AllUserChannelsRequest
import com.banglalink.toffee.data.network.request.AutoRenewRequest
import com.banglalink.toffee.data.network.request.BubbleRequest
import com.banglalink.toffee.data.network.request.CategoryRequest
import com.banglalink.toffee.data.network.request.ContentCategoryRequest
import com.banglalink.toffee.data.network.request.ContentEditRequest
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.request.ContentShareLogRequest
import com.banglalink.toffee.data.network.request.ContentShareableRequest
import com.banglalink.toffee.data.network.request.ContentUploadRequest
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.request.DobValidateOtpRequest
import com.banglalink.toffee.data.network.request.DramaEpisodesBySeasonRequest
import com.banglalink.toffee.data.network.request.DramaSeriesContentRequest
import com.banglalink.toffee.data.network.request.DrmTokenRequest
import com.banglalink.toffee.data.network.request.DrmTokenV1Request
import com.banglalink.toffee.data.network.request.FavoriteContentRequest
import com.banglalink.toffee.data.network.request.FavoriteRequest
import com.banglalink.toffee.data.network.request.FcmTokenRequest
import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.request.FireworkRequest
import com.banglalink.toffee.data.network.request.FmRadioContentRequest
import com.banglalink.toffee.data.network.request.FollowCategoryRequest
import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.request.KeepAliveRequest
import com.banglalink.toffee.data.network.request.LoginByPhoneRequest
import com.banglalink.toffee.data.network.request.LogoutRequest
import com.banglalink.toffee.data.network.request.MediaCdnSignUrlRequest
import com.banglalink.toffee.data.network.request.MnpStatusRequest
import com.banglalink.toffee.data.network.request.MostPopularContentRequest
import com.banglalink.toffee.data.network.request.MostPopularPlaylistsRequest
import com.banglalink.toffee.data.network.request.MovieCategoryDetailRequest
import com.banglalink.toffee.data.network.request.MoviesComingSoonRequest
import com.banglalink.toffee.data.network.request.MoviesPreviewRequest
import com.banglalink.toffee.data.network.request.MqttRequest
import com.banglalink.toffee.data.network.request.MyChannelAddToPlaylistRequest
import com.banglalink.toffee.data.network.request.MyChannelDetailRequest
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.request.MyChannelPlaylistCreateRequest
import com.banglalink.toffee.data.network.request.MyChannelPlaylistDeleteRequest
import com.banglalink.toffee.data.network.request.MyChannelPlaylistEditRequest
import com.banglalink.toffee.data.network.request.MyChannelPlaylistRequest
import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideoDeleteRequest
import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideosRequest
import com.banglalink.toffee.data.network.request.MyChannelRatingRequest
import com.banglalink.toffee.data.network.request.MyChannelSubscribeRequest
import com.banglalink.toffee.data.network.request.MyChannelUserPlaylistRequest
import com.banglalink.toffee.data.network.request.MyChannelUserPlaylistVideosRequest
import com.banglalink.toffee.data.network.request.MyChannelVideoDeleteRequest
import com.banglalink.toffee.data.network.request.MyChannelVideosRequest
import com.banglalink.toffee.data.network.request.NavCategoryRequest
import com.banglalink.toffee.data.network.request.OffenseRequest
import com.banglalink.toffee.data.network.request.PackPaymentMethodRequest
import com.banglalink.toffee.data.network.request.PackVoucherMethodRequest
import com.banglalink.toffee.data.network.request.PackageChannelListRequest
import com.banglalink.toffee.data.network.request.PackageListRequest
import com.banglalink.toffee.data.network.request.PairWithTvRequest
import com.banglalink.toffee.data.network.request.PartnersRequest
import com.banglalink.toffee.data.network.request.PaymentMethodRequest
import com.banglalink.toffee.data.network.request.PlaylistShareableRequest
import com.banglalink.toffee.data.network.request.PopularChannelsRequest
import com.banglalink.toffee.data.network.request.PremiumPackDetailRequest
import com.banglalink.toffee.data.network.request.PremiumPackListRequest
import com.banglalink.toffee.data.network.request.PremiumPackStatusRequest
import com.banglalink.toffee.data.network.request.PremiumPackSubHistoryRequest
import com.banglalink.toffee.data.network.request.ProfileRequest
import com.banglalink.toffee.data.network.request.RechargeByBkashRequest
import com.banglalink.toffee.data.network.request.RedeemReferralCodeRequest
import com.banglalink.toffee.data.network.request.ReferralCodeRequest
import com.banglalink.toffee.data.network.request.ReferralCodeStatusRequest
import com.banglalink.toffee.data.network.request.ReferrerPolicyRequest
import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.request.RemoveTokenizedAccountApiRequest
import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.request.StingrayContentRequest
import com.banglalink.toffee.data.network.request.SubscribePackageRequest
import com.banglalink.toffee.data.network.request.SubscribedUserChannelsRequest
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.request.TermsConditionRequest
import com.banglalink.toffee.data.network.request.TokenizedAccountInfoApiRequest
import com.banglalink.toffee.data.network.request.TokenizedPaymentMethodsApiRequest
import com.banglalink.toffee.data.network.request.UpdateProfileRequest
import com.banglalink.toffee.data.network.request.UploadConfirmationRequest
import com.banglalink.toffee.data.network.request.UploadProfileImageRequest
import com.banglalink.toffee.data.network.request.UploadSignedUrlRequest
import com.banglalink.toffee.data.network.request.VastTagRequestV3
import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.request.ViewingContentRequest
import com.banglalink.toffee.data.network.response.AccountDeleteResponse
import com.banglalink.toffee.data.network.response.AllChannelResponse
import com.banglalink.toffee.data.network.response.AllUserChannelsEditorsChoiceResponse
import com.banglalink.toffee.data.network.response.AllUserChannelsResponse
import com.banglalink.toffee.data.network.response.AutoRenewResponse
import com.banglalink.toffee.data.network.response.BubbleResponse
import com.banglalink.toffee.data.network.response.CategoryResponse
import com.banglalink.toffee.data.network.response.ContentEditResponse
import com.banglalink.toffee.data.network.response.ContentResponse
import com.banglalink.toffee.data.network.response.ContentShareLogResponse
import com.banglalink.toffee.data.network.response.ContentShareableResponse
import com.banglalink.toffee.data.network.response.ContentUploadResponse
import com.banglalink.toffee.data.network.response.DobValidateOtpBaseResponse
import com.banglalink.toffee.data.network.response.DramaEpisodesBySeasonResponse
import com.banglalink.toffee.data.network.response.DramaSeriesContentResponse
import com.banglalink.toffee.data.network.response.DrmTokenResponse
import com.banglalink.toffee.data.network.response.DrmTokenV1Response
import com.banglalink.toffee.data.network.response.FavoriteContentResponse
import com.banglalink.toffee.data.network.response.FavoriteResponse
import com.banglalink.toffee.data.network.response.FcmTokenResponse
import com.banglalink.toffee.data.network.response.FeatureContentResponse
import com.banglalink.toffee.data.network.response.FireworkResponse
import com.banglalink.toffee.data.network.response.FmRadioContentResponse
import com.banglalink.toffee.data.network.response.FollowCategoryResponse
import com.banglalink.toffee.data.network.response.HeaderEnrichmentResponse
import com.banglalink.toffee.data.network.response.HeartBeatResponse
import com.banglalink.toffee.data.network.response.HistoryContentResponse
import com.banglalink.toffee.data.network.response.KeepAliveResponse
import com.banglalink.toffee.data.network.response.LoginByPhoneResponse
import com.banglalink.toffee.data.network.response.LogoutResponse
import com.banglalink.toffee.data.network.response.MediaCdnSignUrlResponse
import com.banglalink.toffee.data.network.response.MnpStatusResponse
import com.banglalink.toffee.data.network.response.MostPopularContentResponse
import com.banglalink.toffee.data.network.response.MostPopularPlaylistsResponse
import com.banglalink.toffee.data.network.response.MovieCategoryDetailResponse
import com.banglalink.toffee.data.network.response.MoviesComingSoonResponse
import com.banglalink.toffee.data.network.response.MoviesPreviewResponse
import com.banglalink.toffee.data.network.response.MqttResponse
import com.banglalink.toffee.data.network.response.MyChannelAddToPlaylistResponse
import com.banglalink.toffee.data.network.response.MyChannelDetailResponse
import com.banglalink.toffee.data.network.response.MyChannelEditResponse
import com.banglalink.toffee.data.network.response.MyChannelPlaylistCreateResponse
import com.banglalink.toffee.data.network.response.MyChannelPlaylistDeleteResponse
import com.banglalink.toffee.data.network.response.MyChannelPlaylistEditResponse
import com.banglalink.toffee.data.network.response.MyChannelPlaylistResponse
import com.banglalink.toffee.data.network.response.MyChannelPlaylistVideoDeleteResponse
import com.banglalink.toffee.data.network.response.MyChannelPlaylistVideosResponse
import com.banglalink.toffee.data.network.response.MyChannelRatingResponse
import com.banglalink.toffee.data.network.response.MyChannelSubscribeResponse
import com.banglalink.toffee.data.network.response.MyChannelVideoDeleteResponse
import com.banglalink.toffee.data.network.response.MyChannelVideosResponse
import com.banglalink.toffee.data.network.response.NavCategoryResponse
import com.banglalink.toffee.data.network.response.OffenseResponse
import com.banglalink.toffee.data.network.response.PackPaymentMethodResponse
import com.banglalink.toffee.data.network.response.PackageChannelListResponse
import com.banglalink.toffee.data.network.response.PackageListResponse
import com.banglalink.toffee.data.network.response.PairWithTvResponse
import com.banglalink.toffee.data.network.response.PartnersResponse
import com.banglalink.toffee.data.network.response.PaymentMethodResponse
import com.banglalink.toffee.data.network.response.PopularChannelsResponse
import com.banglalink.toffee.data.network.response.PremiumPackDetailResponse
import com.banglalink.toffee.data.network.response.PremiumPackListResponse
import com.banglalink.toffee.data.network.response.PremiumPackStatusResponse
import com.banglalink.toffee.data.network.response.PremiumPackSubHistoryResponse
import com.banglalink.toffee.data.network.response.ProfileResponse
import com.banglalink.toffee.data.network.response.RechargeByBkashResponse
import com.banglalink.toffee.data.network.response.RedeemReferralCodeResponse
import com.banglalink.toffee.data.network.response.ReferralCodeResponse
import com.banglalink.toffee.data.network.response.ReferralCodeStatusResponse
import com.banglalink.toffee.data.network.response.ReferrerPolicyResponse
import com.banglalink.toffee.data.network.response.RelativeContentResponse
import com.banglalink.toffee.data.network.response.RemoveTokenizeAccountApiBaseResponse
import com.banglalink.toffee.data.network.response.SearchContentResponse
import com.banglalink.toffee.data.network.response.SubscribePackageResponse
import com.banglalink.toffee.data.network.response.SubscribedUserChannelsResponse
import com.banglalink.toffee.data.network.response.SubscriberPaymentInitResponse
import com.banglalink.toffee.data.network.response.TermsAndConditionResponse
import com.banglalink.toffee.data.network.response.TokenizedAccountInfoApiResponse
import com.banglalink.toffee.data.network.response.TokenizedPaymentMethodsBaseApiResponse
import com.banglalink.toffee.data.network.response.UpdateProfileResponse
import com.banglalink.toffee.data.network.response.UploadConfirmationResponse
import com.banglalink.toffee.data.network.response.UploadProfileImageResponse
import com.banglalink.toffee.data.network.response.UploadSignedUrlResponse
import com.banglalink.toffee.data.network.response.VastTagResponseV3
import com.banglalink.toffee.data.network.response.VerifyCodeResponse
import com.banglalink.toffee.data.network.response.ViewingContentResponse
import com.banglalink.toffee.data.network.response.VoucherPaymentMethodResponse
import com.banglalink.toffee.model.FeaturedPartnerRequest
import com.banglalink.toffee.model.FeaturedPartnerResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

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

    @POST("stingray-contents/${Constants.DEVICE_TYPE}/{type}/{categoryId}/{subcategoryId}/{limit}/{offset}/{dbVersion}")
    //https://staging.toffee-cms.com/contents-v5/deviceType/type/telcoId/categoryId/subCategoryId/limit/offset/dbVersion
    suspend fun getStingrayContents(
        @Path("type") type: String,
        @Path("categoryId") categoryId: Int,
        @Path("subcategoryId") subCategoryId: Int,
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body contentRequest: StingrayContentRequest
    ): ContentResponse

    @POST("ugc-search-content-v2/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
    suspend fun searchContent(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Query("keyword") keyword: String,
        @Body searchContentRequest: SearchContentRequest
    ):SearchContentResponse

    @POST("ugc-fireworks-list/${Constants.DEVICE_TYPE}/NULL/NULL/10/0/{dbVersion}")  //{BaseUrl}/ugc-fireworks-list/deviceType/channel_id/playlist_id/limit/offset/dbVersion
    suspend fun getFireworks(
        @Path("dbVersion") dbVersion: Int,
        @Body fireworkRequest: FireworkRequest
    ): FireworkResponse

    @POST("history-contents")
    suspend fun getHistoryContents(@Body historyContentRequest: HistoryContentRequest):HistoryContentResponse

    @POST("ugc-favorite-contents")
    suspend fun getFavoriteContents(@Body favoriteContentRequest: FavoriteContentRequest):FavoriteContentResponse

    @POST("ugc-app-home-page-content-toffee-v2/${Constants.DEVICE_TYPE}/0/1/200/{dbVersion}")//https://staging.toffee-cms.com/app-home-page-content-toffee-v2/deviceType/subCategoryId/telcoId/limit/dbVesion
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

    @POST("contents-shareable")
    suspend fun getContentFromShareableUrl(@Body shareableRequest: ContentShareableRequest):ContentShareableResponse

    @POST("content-share-log")
    suspend fun sendShareLog(@Body shareableRequest: ContentShareLogRequest):ContentShareLogResponse

    @POST("v2/heart-beat/{dbVersion}")
    suspend fun sendHeartBeat(
        @Path("dbVersion") dbVersion: Int,
        @Body heartBeatRequest: HeartBeatRequest
    ):HeartBeatResponse
    
    @POST
    suspend fun sendKeepAlive(
        @Url url: String,
        @Body keepAliveRequest: KeepAliveRequest
    ):KeepAliveResponse

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

    @POST("v2/redeem-referral-code")
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

    @POST("/account-deletion")
    suspend fun accountDelete(
        @Body AccountDeleteRequest: AccountDeleteRequest
    ): AccountDeleteResponse
    
    @POST("v3/vast-tags-list/${Constants.DEVICE_TYPE}/{dbVersion}")
    suspend fun getVastTagListsV3(
        @Path("dbVersion") dbVersion: Int,
        @Body vastTagRequestV3: VastTagRequestV3
    ): VastTagResponseV3

    @POST
    suspend fun getDrmToken(
        @Url url: String,
        @Header("DRM-API-HEADER") drmHeader: String,
        @Body drmTokenRequest: DrmTokenRequest
    ): DrmTokenResponse
    
    @POST("v1/drm-token/{contentId}")
    suspend fun getDrmTokenV1(
        @Path("contentId") contentId: String,
        @Body drmTokenV1Request: DrmTokenV1Request
    ): DrmTokenV1Response

    @POST("/media-cdn-sign-url")
    suspend fun getMediaCdnSignUrl(
        @Body mediaCdnSignUrlRequest: MediaCdnSignUrlRequest
    ): MediaCdnSignUrlResponse

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
    
    @POST("/playlist-shareable/${Constants.DEVICE_TYPE}/{isUserPlaylist}/{isOwner}/{channelOwnerUserId}/{playlistId}/{limit}/{offset}")
    suspend fun getPlaylistShareable(
        @Path("isUserPlaylist") isUserPlaylist: Int,
        @Path("isOwner") isOwner: Int,
        @Path("channelOwnerUserId") channelOwnerUserId: Int,
        @Path("playlistId") playlistId: Int,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Body playlistRequest: PlaylistShareableRequest
    ): MyChannelPlaylistVideosResponse

    @POST("/ramadan-scheduled/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
    suspend fun getRamadanScheduled(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body bubbleRequest: BubbleRequest
    ): BubbleResponse

    @POST("/data-pack-status/{isBlNumber}/{contentId}/{dbVersion}")
    suspend fun getPremiumStatus(
        @Path("isBlNumber") isBlNumber: Int,
        @Path("contentId") contentId: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body premiumPackStatusRequest: PremiumPackStatusRequest
    ): PremiumPackStatusResponse

    @POST("/premium-packages/{contentId}/{dbVersion}")
    suspend fun getPremiumPackList(
        @Path("contentId") contentId: String,
        @Path("dbVersion") dbVersion: Int,
        @Body premiumPackListRequest: PremiumPackListRequest
    ): PremiumPackListResponse

    @POST("/data-pack-purchase")
    suspend fun purchaseDataPack(
        @Body premiumPackListRequest: DataPackPurchaseRequest
    ): PremiumPackStatusResponse

    @POST("/package-details/{packageId}/{dbVersion}")
    suspend fun getPremiumPackDetail(
        @Path("packageId") packId: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body premiumPackDetailRequest: PremiumPackDetailRequest
    ): PremiumPackDetailResponse

    @POST("/package-wise-data-pack/{isBlNumber}/{packageId}/{dbVersion}")
    suspend fun getPackPaymentMethods(
        @Path("isBlNumber") isBlNumber: Int,
        @Path("packageId") packId: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body packPaymentMethodRequest: PackPaymentMethodRequest
    ): PackPaymentMethodResponse

    @POST("/recharge-initialized")
    suspend fun getRechargeByBkashUrl(
        @Body rechargeByBkashRequest: RechargeByBkashRequest
    ): RechargeByBkashResponse

    @POST("/subscriber/v1/payment/init/{paymentType}")
    suspend fun getSubscriberPaymentInit(
        @Path("paymentType") paymentType: String,
        @Body subscriberPaymentInitRequest: SubscriberPaymentInitRequest
    ): SubscriberPaymentInitResponse

    @POST("/subscriber/v1/tokenized/account/add/init/{paymentType}")
    suspend fun getAddTokenizedAccountInit(
        @Path("paymentType") paymentType: String,
        @Body addTokenizedAccountInitRequest: AddTokenizedAccountInitRequest
    ): SubscriberPaymentInitResponse

    @POST("/mnp-status")
    suspend fun getMnpStatus(
        @Body mnpStatusRequest: MnpStatusRequest
    ): MnpStatusResponse

    @POST("/premium-pack-subscription-history")
    suspend fun getPremiumPackSubscriptionHistory(
        @Body premiumPackSubHistoryRequest: PremiumPackSubHistoryRequest
    ): PremiumPackSubHistoryResponse

    @POST("fm-radio-contents/${Constants.DEVICE_TYPE}/{limit}/{offset}/{dbVersion}")
    suspend fun getFmRadioContents(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int,
        @Path("dbVersion") dbVersion: Int,
        @Body fmRadioContentRequest: FmRadioContentRequest
    ): FmRadioContentResponse

    @POST("check-voucher-status/{packId}/{dbVersion}")
    suspend fun getVoucherPayment(
        @Path("packId") packId:Int,
        @Path("dbVersion") dbVersion: Int,
        @Body packVoucherMethodRequest: PackVoucherMethodRequest
    ): VoucherPaymentMethodResponse


    @POST("device/pair")
    suspend fun pairWithTv(@Body pairWithTvRequest: PairWithTvRequest):PairWithTvResponse

    @POST("/tokenized-payment-manage-methods")
    suspend fun getTokenizedPaymentMethods(
        @Body body: TokenizedPaymentMethodsApiRequest
    ): TokenizedPaymentMethodsBaseApiResponse

    @POST("/tokenized-account-info/{paymentMethodId}")
    suspend fun getTokenizedAccountInfo(
        @Path("paymentMethodId") paymentMethodId:Int,
        @Body body: TokenizedAccountInfoApiRequest
    ): TokenizedAccountInfoApiResponse

    @POST("/remove-tokenized-account/{paymentMethodId}")
    suspend fun removeTokenizeAccount(
        @Path("paymentMethodId") paymentMethodId:Int,
        @Body body: RemoveTokenizedAccountApiRequest
    ) : RemoveTokenizeAccountApiBaseResponse

    @POST("dob-validate-otp")
    suspend fun dobValidateOtp(
        @Body dobValidateOtpRequest: DobValidateOtpRequest
    ): DobValidateOtpBaseResponse

}