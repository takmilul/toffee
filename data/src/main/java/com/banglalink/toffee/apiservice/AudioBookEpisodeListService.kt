package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.HlsLinks
import javax.inject.Inject

class AudioBookEpisodeListService @Inject constructor(
    private val toffeeApi: ExternalApi,
    private val preference: SessionPreference
) {
    suspend fun execute(id : String, token: String): List<ChannelInfo> {
        
        val result = tryIOExternal {
            toffeeApi.audioBookEpisodeList(
                url = "https://api.kabbik.com/v4/toffee/audiobook/$id/${preference.customerId}",
                authorizationToken = "Bearer $token",
                referrer = "https://toffeelive.com/",
            )
        }

        
        val channels = result.episodes.map {
            ChannelInfo(
                id = it.id.toString(),
                program_name = it.name,
                description = it.description,
                authorName = result.authorName,
                duration = it.duration?.replace(".", ":"),
                ugcFeaturedImage = result.bannerPath,
                urlType = 0,
                urlTypeExt = 0,
                type = "Audio_Book",
                is_available = 1,
                is_approved = 1,
                paidPlainHlsUrl = it.filePath,
                hlsLinks = listOf(HlsLinks(it.filePath)),
                is_horizontal = 1,
                playlistContentId = result.id ?: 0,
                playlistName = result.name,
                playlistDescription = result.description,
                contentExpiryTime = "2040-12-31 23:59:59",
            )
        }
        
        return channels
    }
}
