package com.banglalink.toffee.apiservice

import com.banglalink.toffee.model.ChallengeDetail

class GetChallengeDetail {
    suspend fun execute(): ChallengeDetail{
        return ChallengeDetail("Sing Your Song Challenge", "#SingYourSongChallenge", "24d : 16h : 50m remaining", listOf("", "", "", "", ""), "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been \n\nLorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been\n\nLorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been", "1. Must be your own song. Cover songs will not be accepted.\n" +
            "\n" +
            "2. Must be up to 5 minutes of video.\n" +
            "\n" +
            "3. Background music must be copyrighted or permitted by original artist.\n" +
            "\n" +
            "4. Song with abusive language will be removed immediately.", "* Banglalink reserves the right to change challenge submission rules any time.\n" +
            "\n" +
            "* Banglalink reserves the right to change submission duration any time.")
    }
}