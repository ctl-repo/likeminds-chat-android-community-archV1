package com.likeminds.chatmm.media.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
class MediaPickerItemExtras private constructor(
    val bucketId: String,
    val folderTitle: String,
    val mediaTypes: List<String>,
    val allowMultipleSelect: Boolean
) : Parcelable {
    class Builder {
        private var bucketId: String = ""
        private var folderTitle: String = ""
        private var mediaTypes: List<String> = emptyList()
        private var allowMultipleSelect: Boolean = true

        fun bucketId(bucketId: String) = apply { this.bucketId = bucketId }
        fun folderTitle(folderTitle: String) = apply { this.folderTitle = folderTitle }
        fun mediaTypes(mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun allowMultipleSelect(allowMultipleSelect: Boolean) =
            apply { this.allowMultipleSelect = allowMultipleSelect }

        fun build() = MediaPickerItemExtras(bucketId, folderTitle, mediaTypes, allowMultipleSelect)
    }

    fun toBuilder(): Builder {
        return Builder().bucketId(bucketId)
            .folderTitle(folderTitle)
            .mediaTypes(mediaTypes)
            .allowMultipleSelect(allowMultipleSelect)
    }
}