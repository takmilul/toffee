package com.banglalink.toffee.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["channelId"], unique = true)])
data class DrmLicenseEntity(
    @SerialName("channelId")
    @ColumnInfo(defaultValue = "0")
    val channelId: Long = 0,
    @SerialName("contentId")
    val contentId: String? = null,
    @SerialName("license")
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB)
    val license: ByteArray? = null,
    @SerialName("expiryTime")
    @ColumnInfo(defaultValue = "0")
    val expiryTime: Long = 0
) : BaseEntity() {
    
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