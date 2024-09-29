package com.likeminds.chatmm.utils.mediauploader.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.likeminds.chatmm.SDKApplication
import com.likeminds.chatmm.conversation.model.AttachmentViewData
import com.likeminds.chatmm.conversation.model.ConversationViewData
import com.likeminds.chatmm.utils.ViewDataConverter
import com.likeminds.chatmm.utils.mediauploader.model.*
import com.likeminds.chatmm.utils.mediauploader.utils.WorkerUtil.getIntOrNull
import com.likeminds.likemindschat.LMChatClient
import com.likeminds.likemindschat.conversation.model.UpdateConversationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.*

abstract class MediaUploadWorker(
    appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    val lmChatClient = LMChatClient.getInstance()

    protected val transferUtility by lazy { SDKApplication.getInstance().transferUtility }

    protected var uploadedCount = 0
    protected val failedIndex by lazy { ArrayList<Int>() }
    protected lateinit var uploadList: ArrayList<GenericFileRequest>
    protected val thumbnailMediaMap by lazy { HashMap<Int, Pair<String?, String?>>() }
    private val progressMap by lazy { HashMap<Int, Pair<Long, Long>>() }

    protected lateinit var conversation: ConversationViewData

    abstract fun checkArgs()
    abstract fun init()
    abstract fun uploadFiles(continuation: Continuation<Int>)

    companion object {
        const val ARG_MEDIA_INDEX_LIST = "ARG_MEDIA_INDEX_LIST"
        const val ARG_PROGRESS = "ARG_PROGRESS"

        fun getProgress(workInfo: WorkInfo): Pair<Long, Long>? {
            val progress = workInfo.progress.getLongArray(ARG_PROGRESS)
            if (progress == null || progress.size != 2) {
                return null
            }
            return Pair(progress[0], progress[1])
        }
    }

    fun require(key: String) {
        if (!containsParam(key)) {
            throw Error("$key is required")
        }
    }

    override suspend fun doWork(): Result {
        try {
            checkArgs()
            init()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
        return withContext(Dispatchers.IO) {
            val result = suspendCoroutine<Int> { continuation ->
                uploadFiles(continuation)
            }
            return@withContext when (result) {
                WORKER_SUCCESS -> {
                    Log.d("PUI", "worker success called")
                    Log.d(
                        "PUI", """
                        worker success called
                        conversation: ${conversation.id}
                        attachments url:${conversation.attachments?.map { it.url }}
                        attachments uri:${conversation.attachments?.map { it.uri }}
                        attachments thumbnail:${conversation.attachments?.map { it.thumbnail }}
                    """.trimIndent()
                    )
                    Result.success()
                }

                WORKER_RETRY -> {
                    Log.d("PUI", "worker retry called")
                    Result.retry()
                }

                else -> {
                    Log.d("PUI", "worker else called")
                    getFailureResult(failedIndex.toIntArray())
                }
            }
        }
    }

    private fun getFailureResult(failedArrayIndex: IntArray = IntArray(0)): Result {
        return Result.failure(
            Data.Builder()
                .putIntArray(ARG_MEDIA_INDEX_LIST, failedArrayIndex)
                .build()
        )
    }

    protected fun setProgress(id: Int, bytesCurrent: Long, bytesTotal: Long) {
        progressMap[id] = Pair(bytesCurrent, bytesTotal)
        var averageBytesCurrent = 0L
        var averageBytesTotal = 0L
        progressMap.values.forEach {
            averageBytesCurrent += it.first
            averageBytesTotal += it.second
        }
        if (averageBytesCurrent > 0L && averageBytesTotal > 0L) {
            setProgressAsync(
                Data.Builder()
                    .putLongArray(ARG_PROGRESS, longArrayOf(averageBytesCurrent, averageBytesTotal))
                    .build()
            )
        }
    }

    protected fun getStringParam(key: String): String {
        return params.inputData.getString(key)
            ?: throw Error("$key is required")
    }

    protected fun getIntParam(key: String): Int {
        return params.inputData.getIntOrNull(key)
            ?: throw Error("$key is required")
    }

    protected fun containsParam(key: String): Boolean {
        return params.inputData.keyValueMap.containsKey(key)
    }

    protected fun createAWSRequestList(
        thumbnailsToUpload: List<AttachmentViewData>,
        attachmentsToUpload: List<AttachmentViewData>
    ): ArrayList<GenericFileRequest> {
        val awsFileRequestList = ArrayList<GenericFileRequest>()
        thumbnailsToUpload.forEach { attachment ->
            val request = GenericFileRequest.Builder()
                .fileType(attachment.type)
                .awsFolderPath(attachment.thumbnailAWSFolderPath ?: "")
                .localFilePath(attachment.thumbnailLocalFilePath)
                .index(attachment.index ?: 0)
                .isThumbnail(true)
                .build()
            awsFileRequestList.add(request)
        }
        attachmentsToUpload.forEach { attachment ->

            Log.d(
                "PUI", """
                createAWSRequestList
                attachment url: ${attachment.url}
                attachment uri: ${attachment.uri}
                attachment file path: ${attachment.localFilePath}
            """.trimIndent()
            )

            val request = GenericFileRequest.Builder()
                .name(attachment.name)
                .fileType(attachment.type)
                .awsFolderPath(attachment.awsFolderPath ?: "")
                .localFilePath(attachment.localFilePath)
                .index(attachment.index ?: 0)
                .width(attachment.width)
                .height(attachment.height)
                .hasThumbnail(attachment.thumbnailAWSFolderPath != null)
                .meta(attachment.meta)
                .build()
            awsFileRequestList.add(request)
        }
        return awsFileRequestList
    }

    protected fun updateConversation(
        response: AWSFileResponse,
        urls: Pair<String?, String?>,
        totalFileCount: Int,
        continuation: Continuation<Int>
    ) {
        //updateConversation
        val attachments = conversation.attachments ?: return

        val index = attachments.indexOfFirst {
            it.index == response.index
        }

        var attachment = attachments[index]
        attachment = attachment.toBuilder()
            .url(urls.first)
            .uri(Uri.parse(urls.first))
            .thumbnail(urls.second)
            .build()
        attachments[index] = attachment

        conversation = conversation.toBuilder()
            .attachments(attachments)
            .build()

        uploadedCount += 1

        //update local db
        val updateConversationRequest = UpdateConversationRequest.Builder()
            .conversation(ViewDataConverter.convertConversation(conversation))
            .build()
        lmChatClient.updateConversation(updateConversationRequest)

        checkWorkerComplete(totalFileCount, continuation)
    }

    protected fun checkWorkerComplete(
        totalFilesToUpload: Int,
        continuation: Continuation<Int>
    ) {
        Log.d(
            "PUI", """
                     in checkWorkerComplete -> outside it
                    totalfiles: $totalFilesToUpload
                    uploaded: $uploadedCount
                    failed: ${failedIndex.size}
                """.trimIndent()
        )
        if (totalFilesToUpload == uploadedCount + failedIndex.size) {
            Log.d(
                "PUI", """
                     in checkWorkerComplete -> inside if
                    totalfiles: $totalFilesToUpload
                    uploaded: $uploadedCount
                """.trimIndent()
            )
            if (totalFilesToUpload == uploadedCount) {
                continuation.resume(WORKER_SUCCESS)
            } else {
                continuation.resume(WORKER_FAILURE)
            }
        }
    }
}