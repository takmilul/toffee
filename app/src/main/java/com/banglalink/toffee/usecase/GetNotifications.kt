package com.banglalink.toffee.usecase

import com.banglalink.toffee.model.Notification
import com.banglalink.toffee.ui.common.SingleListRepository

class GetNotifications : SingleListRepository<Notification> {
    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<Notification> {
        val notifications: MutableList<Notification> = mutableListOf()
        repeat(limit) {
            notifications.add(Notification("Live Now", "Somebody That I Used To Know (feat. Kimbra) official music video...", null, "23m"))
            notifications.add(Notification("Uploaded Video", "Somebody That I Used To Know (feat. Kimbra) official music video...", null, "43m"))
            notifications.add(Notification("Commented on your video", "Somebody That I Used To Know (feat. Kimbra) official music video...", null, "56m"))
            notifications.add(Notification("Replied to your comment", "Somebody That I Used To Know (feat. Kimbra) official music video...","Hello! This is the expanded view of notification list items. Lorem Ipsum is simply dummy text of theprin and typesetting industry. Lorem Ipsum has been the industry's standard. dummy text ever since the 1500s",  "56m"))
        }
        return notifications
    }
}