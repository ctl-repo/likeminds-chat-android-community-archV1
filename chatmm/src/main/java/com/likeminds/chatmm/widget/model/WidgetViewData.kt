package com.likeminds.chatmm.widget.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class WidgetViewData private constructor(
    val id: String,
    val parentEntityId: String,
    val parentEntityType: String,
    val metadata: String?,
    val createdAt: Long,
    val updatedAt: Long
) : Parcelable {
    class Builder {
        private var id: String = ""
        private var parentEntityId: String = ""
        private var parentEntityType: String = ""
        private var metadata: String? = null
        private var createdAt: Long = 0L
        private var updatedAt: Long = 0L

        fun id(id: String) = apply { this.id = id }
        fun parentEntityId(parentEntityId: String) = apply { this.parentEntityId = parentEntityId }
        fun parentEntityType(parentEntityType: String) =
            apply { this.parentEntityType = parentEntityType }

        fun metadata(metadata: String?) = apply { this.metadata = metadata }
        fun createdAt(createdAt: Long) = apply { this.createdAt = createdAt }
        fun updatedAt(updatedAt: Long) = apply { this.updatedAt = updatedAt }

        fun build() = WidgetViewData(
            id,
            parentEntityId,
            parentEntityType,
            metadata,
            createdAt,
            updatedAt
        )
    }

    fun toBuilder(): Builder {
        return Builder().id(id)
            .parentEntityId(parentEntityId)
            .parentEntityType(parentEntityType)
            .metadata(metadata)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
    }
}