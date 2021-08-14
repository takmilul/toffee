package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(indices = [Index(value = ["channelId"], unique = true)])
data class DrmLicenseEntity(
    val channelId: Long,
    val contentId: String,
    @ColumnInfo(name = "lic", typeAffinity = ColumnInfo.BLOB)
    val license: ByteArray,
    val expiryTime: Long
): BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DrmLicenseEntity

        if (channelId != other.channelId) return false
        if (contentId != other.contentId) return false
        if (!license.contentEquals(other.license)) return false
        if (expiryTime != other.expiryTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channelId.hashCode()
        result = 31 * result + contentId.hashCode()
        result = 31 * result + license.contentHashCode()
        result = 31 * result + expiryTime.hashCode()
        return result
    }
}