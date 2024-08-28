package com.likeminds.chatmm.chatroom.detail.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import androidx.work.*
import com.likeminds.chatmm.LMAnalytics
import com.likeminds.chatmm.SDKApplication
import com.likeminds.chatmm.chatroom.detail.model.*
import com.likeminds.chatmm.chatroom.detail.util.ChatroomUtil
import com.likeminds.chatmm.chatroom.detail.util.ChatroomUtil.getTypeName
import com.likeminds.chatmm.conversation.model.*
import com.likeminds.chatmm.media.MediaRepository
import com.likeminds.chatmm.media.model.*
import com.likeminds.chatmm.member.model.*
import com.likeminds.chatmm.member.util.UserPreferences
import com.likeminds.chatmm.polls.model.*
import com.likeminds.chatmm.utils.*
import com.likeminds.chatmm.utils.ValueUtils.getEmailIfExist
import com.likeminds.chatmm.utils.ValueUtils.getUrlIfExist
import com.likeminds.chatmm.utils.coroutine.*
import com.likeminds.chatmm.utils.file.util.FileUtil
import com.likeminds.chatmm.utils.mediauploader.model.GenericFileRequest
import com.likeminds.chatmm.utils.mediauploader.worker.ConversationMediaUploadWorker
import com.likeminds.chatmm.utils.mediauploader.worker.UploadHelper
import com.likeminds.chatmm.utils.membertagging.model.TagViewData
import com.likeminds.chatmm.utils.model.BaseViewType
import com.likeminds.likemindschat.LMChatClient
import com.likeminds.likemindschat.LMResponse
import com.likeminds.likemindschat.chatroom.model.*
import com.likeminds.likemindschat.community.model.GetMemberRequest
import com.likeminds.likemindschat.conversation.model.*
import com.likeminds.likemindschat.conversation.util.*
import com.likeminds.likemindschat.dm.model.*
import com.likeminds.likemindschat.helper.model.*
import com.likeminds.likemindschat.poll.model.*
import com.likeminds.likemindschat.user.model.MemberBlockState
import com.likeminds.likemindschat.user.model.MemberStateResponse
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject

class ChatroomDetailViewModel @Inject constructor(
    private val sdkPreferences: SDKPreferences,
    private val userPreferences: UserPreferences,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatroomDetail"
        const val CONVERSATIONS_LIMIT = 100
    }

    private val lmChatClient = LMChatClient.getInstance()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    //Contains all chatroom data, community data and more
    lateinit var chatroomDetail: ChatroomDetailViewData

    /**
     * Returns the current member object
     */
    private var currentMemberFromDb: MemberViewData? = null
    var currentMemberDataFromMemberState: MemberViewData? = null

    //to check whether chatroom is loaded for first time when opened through explore feed/deeplinks
    private var chatroomWasNotLoaded = false

    private var isFirstSyncOngoing = false

    private val managementRights: ArrayList<ManagementRightPermissionViewData> by lazy { ArrayList() }

    //default value is set to true, so that initially member can message,
    //but after api response check for the right to respond
    private val _canMemberRespond: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val canMemberRespond: LiveData<Boolean> = _canMemberRespond

    private val _canMemberCreatePoll: MutableLiveData<Boolean> by lazy { MutableLiveData(true) }
    val canMemberCreatePoll: LiveData<Boolean> = _canMemberCreatePoll

    private val _linkOgTags: MutableLiveData<LinkOGTagsViewData?> by lazy { MutableLiveData() }
    val linkOgTags: LiveData<LinkOGTagsViewData?> = _linkOgTags

    private val _chatroomDetailLiveData by lazy { MutableLiveData<ChatroomDetailViewData?>() }
    val chatroomDetailLiveData: LiveData<ChatroomDetailViewData?> = _chatroomDetailLiveData

    //Chatroom, Data, Scroll position
    private val _initialData by lazy { MutableLiveData<InitialViewData?>() }
    val initialData: LiveData<InitialViewData?> = _initialData

    private val _paginatedData by lazy { MutableLiveData<PaginatedViewData>() }
    val paginatedData: LiveData<PaginatedViewData> = _paginatedData

    //Data, Scroll state
    private val _scrolledData by lazy { MutableLiveData<PaginatedViewData>() }
    val scrolledData: LiveData<PaginatedViewData> = _scrolledData

    private val _addOptionResponse by lazy { MutableLiveData<PollViewData?>() }
    val addOptionResponse: LiveData<PollViewData?> = _addOptionResponse

    //To set topic
    private val _setTopicResponse by lazy { MutableLiveData<ConversationViewData>() }
    val setTopicResponse: LiveData<ConversationViewData> = _setTopicResponse

    private val _contentDownloadSettingsLiveData: MutableLiveData<List<String>?> by lazy {
        MutableLiveData<List<String>?>()
    }

    val contentDownloadSettingsLiveData: LiveData<List<String>?> = _contentDownloadSettingsLiveData

    private val _leaveSecretChatroomResponse by lazy { MutableLiveData<Boolean>() }
    val leaveSecretChatroomResponse: LiveData<Boolean> = _leaveSecretChatroomResponse

    private val _deleteConversationsResponse by lazy { MutableLiveData<Int>() }
    val deleteConversationsResponse: LiveData<Int> = _deleteConversationsResponse

    private val _showDM: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val showDM: LiveData<Boolean> by lazy { _showDM }

    var dmRequestText: String = ""

    private val _updatedChatRequestState: MutableLiveData<ChatRequestState> by lazy { MutableLiveData() }
    val updatedChatRequestState: LiveData<ChatRequestState> by lazy { _updatedChatRequestState }

    private val _dmInitiatedForCM: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val dmInitiatedForCM: LiveData<Boolean> by lazy { _dmInitiatedForCM }

    private val _memberBlocked: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val memberBlocked: LiveData<Boolean> by lazy { _memberBlocked }

    private var sendLinkPreview = true

    sealed class ConversationEvent {
        data class NewConversation(val conversations: List<ConversationViewData>) :
            ConversationEvent()

        data class UpdatedConversation(val conversations: List<ConversationViewData>) :
            ConversationEvent()

        data class PostedConversation(val conversation: ConversationViewData) :
            ConversationEvent()
    }

    private val conversationEventChannel = Channel<ConversationEvent>(Channel.BUFFERED)
    val conversationEventFlow = conversationEventChannel.receiveAsFlow()

    //Returns Triple (poll response, poll info data)
    private val _pollAnswerUpdated: MutableLiveData<PollInfoData?> by lazy { MutableLiveData() }
    val pollAnswerUpdated: LiveData<PollInfoData?> = _pollAnswerUpdated

    private val errorEventChannel = Channel<ErrorMessageEvent>(Channel.BUFFERED)
    val errorMessageFlow = errorEventChannel.receiveAsFlow()

    //Job for preview link API's calls
    private var previewLinkJob: Job? = null

    //Variable to hold current preview link, helps to avoid duplicate API calls
    private var previewLink: String? = null

    sealed class ErrorMessageEvent {
        data class FollowChatroom(val errorMessage: String?) : ErrorMessageEvent()
        data class LeaveSecretChatroom(val errorMessage: String?) : ErrorMessageEvent()
        data class MuteChatroom(val errorMessage: String?) : ErrorMessageEvent()
        data class SetChatroomTopic(val errorMessage: String?) : ErrorMessageEvent()
        data class PostConversation(val errorMessage: String?) : ErrorMessageEvent()
        data class EditConversation(val errorMessage: String?) : ErrorMessageEvent()
        data class DeleteConversation(val errorMessage: String?) : ErrorMessageEvent()
        data class SubmitPoll(val errorMessage: String?) : ErrorMessageEvent()
        data class AddPollOption(val errorMessage: String?) : ErrorMessageEvent()
        data class EditChatroomTitle(val errorMessage: String?) : ErrorMessageEvent()
        data class SendDMRequest(val errorMessage: String?) : ErrorMessageEvent()
        data class BlockMember(val errorMessage: String?) : ErrorMessageEvent()
    }

    private fun getChatroom() = chatroomDetail.chatroom

    fun getChatroomActions(): List<ChatroomActionViewData>? = chatroomDetail.actions

    private fun getConversationListShimmerView() = ConversationListShimmerViewData()

    /**
     * @return
     * True is you are not admin in Announcement Room
     * False if you are admin in Announcement Room
     * False if it's not a Announcement Room
     */
    fun isNotAdminInAnnouncementRoom(): Boolean {
        return if (this::chatroomDetail.isInitialized &&
            chatroomDetail.chatroom?.type == TYPE_ANNOUNCEMENT
        ) {
            !(MemberState.isAdmin(currentMemberFromDb?.state))
        } else {
            false
        }
    }

    fun isDmChatroom(): Boolean {
        if (!this::chatroomDetail.isInitialized) {
            return false
        }
        return getChatroom()?.type == TYPE_DIRECT_MESSAGE
    }

    fun isVoiceNoteSupportEnabled(): Boolean {
        return sdkPreferences.isVoiceNoteSupportEnabled()
    }

    fun isAudioSupportEnabled(): Boolean {
        return sdkPreferences.isAudioSupportEnabled()
    }

    fun isGifSupportEnabled(): Boolean {
        return sdkPreferences.isGifSupportEnabled()
    }

    fun isMicroPollsEnabled(): Boolean {
        return sdkPreferences.isMicroPollsEnabled() && hasCreatePollRights()
    }

    fun isWidgetEnabled(): Boolean {
        return sdkPreferences.getIsWidgetEnabled()
    }

    fun isAnnouncementChatroom(): Boolean {
        return ChatroomType.isAnnouncementRoom(chatroomDetail.chatroom?.type)
    }

    fun canMembersCanMessage(): Boolean? {
        return chatroomDetail.chatroom?.memberCanMessage
    }

    private fun hasCreatePollRights(): Boolean {
        return canMemberCreatePoll.value == true
    }

    /**
     * Is the current member admin of the current chatroom
     */
    fun isAdminMember(): Boolean {
        return MemberState.isAdmin(currentMemberDataFromMemberState?.state)
    }

    fun hasMemberRespondRight(): Boolean {
        return _canMemberRespond.value == true
    }

    fun hasDeleteChatRoomRight(): Boolean {
        return CommunityRightsUtil.hasDeleteChatRoomRight(managementRights)
    }

    fun hasEditCommunityDetailRight(): Boolean {
        return CommunityRightsUtil.hasEditCommunityDetailRight(managementRights)
    }

    fun getChatroomViewData(): ChatroomViewData? {
        return if (this::chatroomDetail.isInitialized) {
            chatroomDetail.chatroom
        } else {
            null
        }
    }

    fun getCurrentTopic(): ConversationViewData? {
        return getChatroom()?.topic
    }

    private fun isChatroomCreator(): Boolean {
        return getChatroomViewData()?.memberViewData?.sdkClientInfo?.uuid == userPreferences.getUUID()
    }

    /**
     * is current member can set a message as Chatroom Topic
     * Only allow when [User] is CM of the Community or he/she is the creator of chatroom
     **/
    fun canSetChatroomTopic(): Boolean {
        return isAdminMember() || isChatroomCreator()
    }

    fun getOtherDmMember(): MemberViewData? {
        return if (
            userPreferences.getUUID() == getChatroom()?.chatroomWithUser?.sdkClientInfo?.uuid
        ) {
            getChatroom()?.memberViewData
        } else {
            getChatroom()?.chatroomWithUser
        }
    }

    fun getLoggedInMemberId(): String {
        return userPreferences.getMemberId()
    }

    //gets the member with provided [uuid] from DB
    fun getMemberFromDB(uuid: String): MemberViewData {
        val memberRequest = GetMemberRequest.Builder()
            .uuid(uuid)
            .build()

        return ViewDataConverter.convertMember(lmChatClient.getMember(memberRequest).data?.member)
    }

    //get first normal or poll conversation for list
    fun getFirstNormalOrPollConversation(items: List<BaseViewType>): ConversationViewData? {
        return items.firstOrNull {
            it is ConversationViewData && ConversationState.isPollOrNormal(it.state)
        } as? ConversationViewData
    }

    fun fetchUriDetails(
        context: Context,
        uris: List<Uri>,
        callback: (media: List<MediaViewData>) -> Unit,
    ) {
        mediaRepository.getLocalUrisDetails(context, uris, callback)
    }

    fun syncChatroom(
        context: Context,
        chatroomId: String
    ): Pair<MediatorLiveData<WorkInfo.State>, Boolean> {
        val getChatroomRequest = GetChatroomRequest.Builder()
            .chatroomId(chatroomId)
            .build()
        val getChatroomResponse = lmChatClient.getChatroom(getChatroomRequest)
        val chatroom = getChatroomResponse.data?.chatroom

        //if conversation is stored for the first time
        //then start reopen sync
        //else first sync
        return if (chatroom?.isConversationStored == true) {
            //reopen worker
            setIsFirstTimeSync(false)
            val worker = lmChatClient.loadConversations(
                context,
                LoadConversationType.REOPEN,
                chatroomId
            )
            Pair(worker, false)
        } else {
            setIsFirstTimeSync(true)
            val worker = lmChatClient.loadConversations(
                context,
                LoadConversationType.FIRST_TIME,
                chatroomId
            )
            Pair(worker, true)
        }
    }

    fun setIsFirstTimeSync(value: Boolean) {
        isFirstSyncOngoing = value
    }

    fun startBackgroundFirstSync(
        context: Context,
        chatroomId: String
    ): MediatorLiveData<WorkInfo.State> {
        return lmChatClient.loadConversations(
            context,
            LoadConversationType.FIRST_TIME_BACKGROUND,
            chatroomId
        )
    }

    /**
     * Fetches the initial data for the current chatroom and pass it to fragment using live data
     * @param context context from where funciton is called
     * @param chatroomDetailExtras Chatroom Intent Extras
     *
     * 1st case ->
     * chatroom is not present
     *
     * 2nd case ->
     * chatroom is present but chatroom is deleted
     *
     * 3rd case ->
     * open a conversation directly through search/deep links
     *
     * 4th case ->
     * chatroom is present and conversation is not present
     *
     * 5th case ->
     * chatroom is opened through deeplink/explore feed, which is open for the first time
     *
     * 6th case ->
     * chatroom is present and conversation is present, chatroom opened for the first time from home feed
     *
     * 7th case ->
     * chatroom is present and conversation is present, chatroom has no unseen conversations
     *
     * 8th case ->
     * chatroom is present and conversation is present, chatroom has unseen conversations
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun getInitialData(chatroomDetailExtras: ChatroomDetailExtras) {
        viewModelScope.launchIO {
            val request =
                GetChatroomRequest.Builder().chatroomId(chatroomDetailExtras.chatroomId).build()
            val getChatroomResponse = lmChatClient.getChatroom(request)
            val chatroom = getChatroomResponse.data?.chatroom
            val dataList = mutableListOf<BaseViewType>()
            //1st case -> chatroom is not present, if yes return
            if (chatroom == null) {
                Log.d(TAG, "case 1")
                chatroomWasNotLoaded = true
                dataList.add(getConversationListShimmerView())
                sendInitialDataToUI(
                    InitialViewData.Builder()
                        .data(dataList)
                        .build()
                )
                return@launchIO
            }

            //2nd case -> chatroom is deleted, if yes return
            if (chatroom.deletedBy != null) {
                Log.d(TAG, "case 2")
                sendInitialDataToUI()
                return@launchIO
            }

            val getMemberRequest = GetMemberRequest.Builder()
                .uuid(userPreferences.getUUID())
                .build()
            currentMemberFromDb =
                ViewDataConverter.convertMember(lmChatClient.getMember(getMemberRequest).data?.member)

            val chatroomViewData = ViewDataConverter.convertChatroom(
                chatroom,
                userPreferences.getUUID(),
                ChatroomUtil.getChatroomViewType(chatroom)
            ) ?: return@launchIO
            chatroomDetail = ChatroomDetailViewData.Builder()
                .chatroom(chatroomViewData)
                .build()

            _canMemberRespond.postValue(true)

            val medianConversationId = if (!chatroomDetailExtras.conversationId.isNullOrEmpty()) {
                chatroomDetailExtras.conversationId
            } else if (!chatroomDetailExtras.reportedConversationId.isNullOrEmpty()) {
                chatroomDetailExtras.reportedConversationId
            } else {
                null
            }

            val initialData = when {
                //3rd case -> open a conversation directly through search/deep links
                medianConversationId != null -> {
                    Log.d(TAG, "case 3")
                    dataList.addAll(
                        fetchIntermediateConversations(
                            chatroomViewData,
                            medianConversationId = medianConversationId
                        )
                    )
                    dataList.addAll(getActionViewList())
                    val scrollIndex =
                        getFirstTimeScrollIndex(chatroom.lastSeenConversation, dataList)
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .scrollPosition(scrollIndex)
                        .build()
                }
                //4th case -> chatroom is present and conversation is not present
                chatroomViewData.totalAllResponseCount == 0 -> {
                    Log.d(TAG, "case 4")
                    dataList.add(getDateView(chatroomViewData.date))
                    dataList.add(chatroomViewData)
                    dataList.addAll(getActionViewList())
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .scrollPosition(SCROLL_UP)
                        .build()
                }

                //5th case -> chatroom is opened through deeplink/explore feed, which is open for the first time
                chatroomWasNotLoaded -> {
                    Log.d(TAG, "case 5")
                    dataList.addAll(fetchBottomConversations(chatroomViewData))
                    dataList.addAll(getActionViewList())
                    chatroomWasNotLoaded = false
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .scrollPosition(SCROLL_DOWN)
                        .build()
                }

                //6th case -> chatroom is present and conversation is present, chatroom opened for the first time from home feed
                chatroom.lastSeenConversation == null || chatroomDetailExtras.loadFromTop == true -> {
                    Log.d(TAG, "case 6")
                    dataList.add(getConversationListShimmerView())
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .build()
                }
                //7th case -> chatroom is present but conversations are not stored in chatroom
                !chatroom.isConversationStored -> {
                    Log.d(TAG, "case 7")
                    dataList.add(getConversationListShimmerView())
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .build()
                }
                //8th case -> chatroom is present and conversation is present, chatroom has no unseen conversations
                chatroom.unseenCount == 0 -> {
                    Log.d(TAG, "case 8")
                    dataList.addAll(fetchBottomConversations(chatroomViewData))
                    dataList.addAll(getActionViewList())
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .scrollPosition(SCROLL_DOWN)
                        .build()
                }
                //9th case -> chatroom is present and conversation is present, chatroom has unseen conversations
                else -> {
                    Log.d(TAG, "case 9")
                    dataList.addAll(
                        fetchIntermediateConversations(
                            chatroomViewData,
                            medianConversation = chatroom.lastSeenConversation
                        )
                    )
                    dataList.addAll(getActionViewList())
                    val scrollIndex =
                        getFirstTimeScrollIndex(chatroom.lastSeenConversation, dataList)
                    InitialViewData.Builder()
                        .chatroom(chatroomViewData)
                        .data(dataList)
                        .scrollPosition(scrollIndex)
                        .build()
                }
            }
            sendInitialDataToUI(initialData)
            fetchChatroomFromNetwork()
            markChatroomAsRead(chatroomDetailExtras.chatroomId)
            fetchMemberState()
            observeConversations(chatroomDetailExtras.chatroomId)
        }
    }

    private fun sendInitialDataToUI(data: InitialViewData? = null) {
        viewModelScope.launchMain {
            _initialData.value = data
        }
    }

    /**
     * Returns the data list to show on the chatroom screen recyclerview
     * @return Pair(Data List, Scroll Position)
     */
    private fun getActionViewList(): List<BaseViewType> {
        val dataList = mutableListOf<BaseViewType>()
        val chatroom = getChatroom()
        when {
            chatroom?.showFollowAutoTag == true -> {
                dataList.add(AutoFollowedTaggedActionViewData())
            }

            chatroom?.showFollowTelescope == true -> {
                dataList.add(FollowItemViewData())
            }
        }
        return dataList
    }

    /**
     * Returns the recyclerview position to show when the user opens the chatroom
     * We always show the last seen conversation at the bottom of the screen in visible portion
     * @param lastSeenConversation The last seen conversation
     * @param items The final recyclerview items
     */
    private fun getFirstTimeScrollIndex(
        lastSeenConversation: Conversation?,
        items: List<BaseViewType>,
    ): Int {
        return items.indexOfFirst {
            it is ConversationViewData && it.id == lastSeenConversation?.id
        }
    }

    /**
     * chatroom is present and conversation is present, chatroom has unseen conversations
     * 1. Fetch the chatroom
     * 2. Find the median (unseen conversation timestamp) from chatroom property
     * 3. Fetch 50 conversations >= than the median timestamp, sorted by ascending on TIMESTAMP | ID and append it at bottom
     * 4. Fetch 50 conversations <= than the median timestamp, sorted by descending on TIMESTAMP | ID , reverse the list and append it at top
     * 5. If the chatroom contains conversations <= 101, then append the chatroom at the top, else don't append
     */
    private fun fetchIntermediateConversations(
        chatroomViewData: ChatroomViewData,
        medianConversation: Conversation? = null,
        medianConversationId: String? = null,
    ): List<BaseViewType> {
        val getConversationRequest = GetConversationRequest.Builder()
            .conversationId(medianConversationId ?: medianConversation?.id ?: "")
            .build()
        val getConversationResponse = lmChatClient.getConversation(getConversationRequest)
        val median = medianConversation
            ?: getConversationResponse.data?.conversation
            ?: return emptyList()
        val medianViewData = ViewDataConverter.convertConversation(median) ?: return emptyList()

        val dataList = mutableListOf<BaseViewType>()

        val getAboveConversationsRequest = GetConversationsRequest.Builder()
            .conversation(median)
            .chatroomId(chatroomViewData.id)
            .type(GetConversationType.ABOVE)
            .limit(CONVERSATIONS_LIMIT)
            .build()
        val getAboveConversationsResponse =
            lmChatClient.getConversations(getAboveConversationsRequest)
        val aboveConversations = getAboveConversationsResponse.data?.conversations ?: emptyList()
        val aboveConversationsViewData = ViewDataConverter.convertConversations(aboveConversations)

        val getBelowConversationsRequest = GetConversationsRequest.Builder()
            .conversation(median)
            .chatroomId(chatroomViewData.id)
            .type(GetConversationType.BELOW)
            .limit(CONVERSATIONS_LIMIT)
            .build()
        val getBelowConversationsResponse =
            lmChatClient.getConversations(getBelowConversationsRequest)
        val belowConversations = getBelowConversationsResponse.data?.conversations ?: emptyList()
        val belowConversationsViewData = ViewDataConverter.convertConversations(belowConversations)

        var conversations =
            (aboveConversationsViewData + medianViewData + belowConversationsViewData)

        if (aboveConversationsViewData.size < CONVERSATIONS_LIMIT
            || aboveConversationsViewData.firstOrNull()?.state == STATE_HEADER
        ) {
            dataList.add(chatroomViewData)
            val headerConversation = getHeaderConversation(conversations)
            if (headerConversation != null) {
                dataList.add(0, headerConversation)
                conversations = conversations.drop(1)
            }
        } else {
            val aboveTopConversationsCount =
                getConversationsAboveCount(aboveConversationsViewData.firstOrNull())
            if (aboveTopConversationsCount == 0) {
                dataList.add(chatroomViewData)
                val headerConversation = getHeaderConversation(conversations)
                if (headerConversation != null) {
                    dataList.add(0, headerConversation)
                    conversations = conversations.drop(1)
                }
            }
        }
        dataList.addAll(
            addDateViewToList(
                conversations,
                chatroomViewData,
                null
            )
        )
        return dataList
    }

    /**
     * chatroom is present and conversation is present, chatroom has no unseen conversations
     * ---chatroom will load from bottom, if conversations are limited, also add chatroom object---
     * 1. Fetch the chatroom and only append it if conversations count <= 50
     * 2. Fetch 50 conversations sorted by descending on TIMESTAMP | ID, reverse the list and append it at bottom
     */
    private fun fetchBottomConversations(
        chatroomViewData: ChatroomViewData,
    ): List<BaseViewType> {
        val dataList = mutableListOf<BaseViewType>()
        val getBottomConversationsRequest = GetConversationsRequest.Builder()
            .chatroomId(chatroomViewData.id)
            .type(GetConversationType.BOTTOM)
            .limit(CONVERSATIONS_LIMIT)
            .build()
        val getBottomConversationsResponse =
            lmChatClient.getConversations(getBottomConversationsRequest)
        val bottomConversations = getBottomConversationsResponse.data?.conversations ?: emptyList()


        var bottomConversationsViewData =
            ViewDataConverter.convertConversations(bottomConversations)

        if (chatroomViewData.totalAllResponseCount <= CONVERSATIONS_LIMIT) {
            //All conversations are fetched
            dataList.add(getDateView(chatroomViewData.date))
            dataList.add(chatroomViewData)
            val headerConversation = getHeaderConversation(bottomConversationsViewData)
            if (headerConversation != null) {
                dataList.add(0, headerConversation)
                bottomConversationsViewData = bottomConversationsViewData.drop(1)
            }
        }
        dataList.addAll(
            addDateViewToList(
                bottomConversationsViewData,
                chatroomViewData,
                null
            )
        )
        return dataList
    }

    /**
     * Observe current chatroom conversations
     * @param chatroomId
     */
    private fun observeConversations(chatroomId: String) {
        viewModelScope.launchMain {
            val conversationChangeListener = object : ConversationChangeListener {
                override fun getChangedConversations(conversations: List<Conversation>?) {
                    if (!conversations.isNullOrEmpty()) {
                        val conversationsViewData =
                            ViewDataConverter.convertConversations(conversations)
                        sendConversationUpdatesToUI(conversationsViewData)
                    }
                }

                override fun getNewConversations(conversations: List<Conversation>?) {
                    if (!conversations.isNullOrEmpty()) {
                        val conversationsViewData =
                            ViewDataConverter.convertConversations(conversations)
                        sendNewConversationsToUI(conversationsViewData)
                    }
                }

                override fun getPostedConversations(conversations: List<Conversation>?) {
                    if (!conversations.isNullOrEmpty()) {
                        val conversationsViewData =
                            ViewDataConverter.convertConversations(conversations)
                        sendConversationUpdatesToUI(conversationsViewData)
                    }
                }
            }

            val observeConversationsRequest = ObserveConversationsRequest.Builder()
                .chatroomId(chatroomId)
                .listener(conversationChangeListener)
                .build()
            lmChatClient.observeConversations(observeConversationsRequest)
        }
    }

    // starts observing live conversations
    fun observeLiveConversations(context: Context, chatroomId: String) {
        viewModelScope.launchIO {
            lmChatClient.observeLiveConversations(context, chatroomId)
        }
    }

    /**
     * Send updates to UI using live data
     * @param conversations List of conversations
     */
    private fun sendConversationUpdatesToUI(conversations: List<ConversationViewData>) {
        val value = conversations.sortedBy {
            it.createdEpoch
        }

        viewModelScope.launchDefault {
            conversationEventChannel.send(ConversationEvent.UpdatedConversation(value))
        }
    }

    /**
     * Send updates to UI using live data
     * @param conversations List of conversations
     */
    private fun sendNewConversationsToUI(conversations: List<ConversationViewData>) {
        if (isFirstSyncOngoing) return
        val value = conversations.sortedBy {
            it.createdEpoch
        }
        viewModelScope.launchDefault {
            conversationEventChannel.send(ConversationEvent.NewConversation(value))
        }
    }

    /**
     * Fetch the top conversations on header click
     */
    fun fetchTopConversationsOnClick(
        topConversation: ConversationViewData,
        oldConversations: List<BaseViewType>,
        repliedChatRoomId: String? = null,
    ) {
        viewModelScope.launchIO {
            val topConversationsCount = getConversationsAboveCount(topConversation)
            if (topConversationsCount <= CONVERSATIONS_LIMIT) {
                val data = fetchPaginatedData(
                    SCROLL_UP,
                    topConversation,
                    oldConversations,
                    SCROLL_UP,
                    repliedChatRoomId = repliedChatRoomId
                )
                if (data != null) {
                    viewModelScope.launchMain {
                        _paginatedData.value = data
                    }
                }
            } else {
                val conversations = fetchTopConversations(getChatroomViewData())
                viewModelScope.launchMain {
                    _scrolledData.value = PaginatedViewData.Builder()
                        .scrollState(SCROLL_UP)
                        .data(conversations)
                        .repliedChatRoomId(repliedChatRoomId)
                        .build()
                }
            }
        }
    }

    /**
     * Fetch the top conversations on header click
     */
    fun fetchBottomConversationsOnClick(
        bottomConversation: ConversationViewData,
        oldConversations: List<BaseViewType>,
    ) {
        viewModelScope.launchIO {
            val bottomConversationsCount = getConversationsBelowCount(bottomConversation)
            if (bottomConversationsCount <= CONVERSATIONS_LIMIT) {
                val data = fetchPaginatedData(
                    SCROLL_DOWN,
                    bottomConversation,
                    oldConversations,
                    SCROLL_DOWN
                )
                if (data != null) {
                    viewModelScope.launchMain {
                        _paginatedData.value = data
                    }
                }
            } else {
                val conversations = fetchBottomConversations(getChatroomViewData()!!)
                viewModelScope.launchMain {
                    _scrolledData.value = PaginatedViewData.Builder()
                        .scrollState(SCROLL_DOWN)
                        .data(conversations)
                        .build()
                }
            }
        }
    }

    fun fetchPaginatedDataOnScroll(
        @ScrollState scrollState: Int,
        conversationKey: ConversationViewData,
        oldConversations: List<BaseViewType>,
    ) {
        viewModelScope.launchIO {
            val data = fetchPaginatedData(
                scrollState,
                conversationKey,
                oldConversations
            )
            if (data != null) {
                viewModelScope.launchMain {
                    _paginatedData.value = data
                }
            }
        }
    }

    private fun fetchPaginatedData(
        @ScrollState scrollState: Int,
        conversationKey: ConversationViewData,
        oldConversations: List<BaseViewType>,
        @ScrollState extremeScrollTo: Int? = null,
        repliedConversationId: String? = null,
        repliedChatRoomId: String? = null,
    ): PaginatedViewData? {
        val chatroom = getChatroom() ?: return null
        val chatroomId = chatroom.id
        val conversationForRequest = ViewDataConverter.convertConversation(conversationKey)
        val getConversationsResponse = if (scrollState == SCROLL_UP) {
            lmChatClient.getConversations(
                GetConversationsRequest.Builder()
                    .chatroomId(chatroomId)
                    .conversation(conversationForRequest)
                    .type(GetConversationType.ABOVE)
                    .limit(CONVERSATIONS_LIMIT)
                    .build()
            )
        } else {
            lmChatClient.getConversations(
                GetConversationsRequest.Builder()
                    .chatroomId(chatroomId)
                    .conversation(conversationForRequest)
                    .type(GetConversationType.BELOW)
                    .limit(CONVERSATIONS_LIMIT)
                    .build()
            )
        }
        val conversations = getConversationsResponse.data?.conversations ?: emptyList()
        var conversationsViewData = ViewDataConverter.convertConversations(conversations)
        if (conversationsViewData.isNotEmpty()) {
            /**
             * If there are no more conversations, and is scrolling up, add the chatroom to the list
             * Also add the header conversation at the top of the chatroom item
             */
            val oldConversationsCount = getConversationsCount(oldConversations)
            val totalConversationsCount = oldConversationsCount + conversationsViewData.size
            val totalAllResponseCount = chatroom.totalAllResponseCount
            return if (
                scrollState == SCROLL_UP &&
                totalConversationsCount == totalAllResponseCount
            ) {
                val dataList = mutableListOf<BaseViewType>()
                dataList.add(getDateView(chatroom.date))
                dataList.add(chatroom)
                val header = getHeaderConversation(conversationsViewData)
                if (header != null) {
                    dataList.add(0, header)
                    conversationsViewData = conversationsViewData.drop(1)
                }
                dataList.addAll(
                    addDateViewToList(
                        conversationsViewData,
                        chatroomDetail.chatroom,
                        null
                    )
                )
                PaginatedViewData.Builder()
                    .scrollState(scrollState)
                    .data(dataList)
                    .extremeScrollTo(extremeScrollTo)
                    .repliedChatRoomId(repliedChatRoomId)
                    .repliedConversationId(repliedConversationId)
                    .build()
            } else {
                PaginatedViewData.Builder()
                    .scrollState(scrollState)
                    .data(
                        addDateViewToList(
                            conversationsViewData,
                            chatroomDetail.chatroom,
                            oldConversations.lastOrNull()
                        )
                    )
                    .extremeScrollTo(extremeScrollTo)
                    .repliedChatRoomId(repliedChatRoomId)
                    .repliedConversationId(repliedConversationId)
                    .build()
            }
        }
        return null
    }

    private fun getConversationsCount(items: List<BaseViewType>) = items.count {
        it is ConversationViewData
    }

    /**
     * chatroom is present and conversation is present, chatroom opened for the first time
     * ---chatroom will load from top with chatroom object and limited conversations---
     * 1. Fetch the chatroom and append it at top
     * 2. Fetch limited conversations sorted by ascending on TIMESTAMP & ID and append it at bottom
     */
    private fun fetchTopConversations(
        chatroom: ChatroomViewData?,
    ): List<BaseViewType> {
        if (chatroom == null) {
            return emptyList()
        }
        val dataList = mutableListOf<BaseViewType>()
        dataList.add(getDateView(chatroom.date))
        dataList.add(chatroom)
        val getConversationsResponse = lmChatClient.getConversations(
            GetConversationsRequest.Builder()
                .chatroomId(chatroom.id)
                .type(GetConversationType.TOP)
                .limit(CONVERSATIONS_LIMIT)
                .build()
        )
        val topConversations = getConversationsResponse.data?.conversations ?: emptyList()
        var topConversationsViewData = ViewDataConverter.convertConversations(topConversations)
        val headerConversation = getHeaderConversation(topConversationsViewData)
        if (headerConversation != null) {
            dataList.add(0, headerConversation)
            topConversationsViewData = topConversationsViewData.drop(1)
        }
        dataList.addAll(
            addDateViewToList(topConversationsViewData, chatroom, null)
        )
        return dataList
    }

    private fun getHeaderConversation(
        conversations: List<BaseViewType>,
    ): ConversationViewData? {
        return conversations.firstOrNull { item ->
            item is ConversationViewData && item.state == STATE_HEADER
        } as? ConversationViewData
    }

    /**
     * Function to insert date view inside a conversations list
     * @param conversations list of conversation
     * @param chatroomViewData chatroom object
     * @param lastItem The last item of the list after which the [conversations] will be appended
     */
    private fun addDateViewToList(
        conversations: List<ConversationViewData>,
        chatroomViewData: ChatroomViewData? = null,
        lastItem: BaseViewType? = null,
    ): List<BaseViewType> {
        val dataList = mutableListOf<BaseViewType>()
        conversations.withIndex().forEach { item ->
            val previousIndex = item.index - 1
            if (previousIndex > -1) {
                val previousConversation = conversations[previousIndex]
                val currentConversation = conversations[item.index]
                if (previousConversation.date != currentConversation.date) {
                    //add date if 2 consecutive conversations have different date value
                    dataList.add(getDateView(currentConversation.date))
                }
                //add the conversation
                dataList.add(item.value)
            } else {
                when {
                    lastItem is ConversationViewData -> {
                        if (lastItem.date != item.value.date) {
                            dataList.add(getDateView(item.value.date))
                        }
                    }

                    lastItem is ChatroomDateViewData -> {
                        if (lastItem.date != item.value.date) {
                            dataList.add(getDateView(item.value.date))
                        }
                    }

                    chatroomViewData?.date != item.value.date -> {
                        dataList.add(getDateView(item.value.date))
                    }
                }
                //add the first conversation
                dataList.add(item.value)
            }
        }
        return dataList
    }

    fun isShimmerPresent(items: List<BaseViewType>): Boolean {
        return items.firstOrNull {
            it is ConversationListShimmerViewData
        } != null
    }

    fun getDateView(date: String?): ChatroomDateViewData {
        return ChatroomDateViewData.Builder()
            .date(date)
            .build()
    }

    fun createTemporaryAutoFollowAndTopicConversation(
        state: Int,
        message: String,
    ): ConversationViewData {
        val memberViewData = currentMemberFromDb ?: MemberViewData.Builder().build()
        return ConversationViewData.Builder()
            .id("-")
            .state(state)
            .memberViewData(memberViewData)
            .createdEpoch(System.currentTimeMillis())
            .answer(
                "${
                    Route.createRouteForMemberProfile(
                        currentMemberFromDb,
                        getChatroom()?.communityId
                    )
                } $message"
            )
            .build()
    }

    fun getFirstConversationFromAdapter(items: List<BaseViewType>): ConversationViewData? {
        return items.firstOrNull {
            it is ConversationViewData
        } as? ConversationViewData
    }

    fun getLastConversationFromAdapter(items: List<BaseViewType>): ConversationViewData? {
        return items.lastOrNull {
            it is ConversationViewData
        } as? ConversationViewData
    }

    fun isAllBottomConversationsAdded(bottomConversation: ConversationViewData?): Boolean {
        return getConversationsBelowCount(bottomConversation) == 0
    }

    private fun getConversationsAboveCount(
        aboveConversation: ConversationViewData?,
    ): Int {
        if (aboveConversation == null) {
            return 0
        }
        val conversation = ViewDataConverter.convertConversation(aboveConversation)
        val getConversationsCountRequest = GetConversationsCountRequest.Builder()
            .chatroomId(getChatroom()?.id ?: "")
            .conversation(conversation)
            .type(GetConversationCountType.ABOVE)
            .build()
        return lmChatClient.getConversationsCount(getConversationsCountRequest).data?.count ?: 0
    }


    private fun getConversationsBelowCount(
        bottomConversation: ConversationViewData?,
    ): Int {
        if (bottomConversation == null) {
            return 0
        }
        val conversation = ViewDataConverter.convertConversation(bottomConversation)
        val getConversationsCountRequest = GetConversationsCountRequest.Builder()
            .chatroomId(getChatroom()?.id ?: "")
            .conversation(conversation)
            .type(GetConversationCountType.BELOW)
            .build()
        return lmChatClient.getConversationsCount(getConversationsCountRequest).data?.count ?: 0
    }

    fun getContentDownloadSettings() {
        viewModelScope.launchIO {
            val response = lmChatClient.getContentDownloadSettings()
            if (response.success) {
                val data = response.data ?: return@launchIO
                val settings = data.settings
                val optionsDownloadable = settings.filter { it.enabled }
                val contentTypes = optionsDownloadable.map {
                    it.downloadSettingType
                }
                _contentDownloadSettingsLiveData.postValue(contentTypes)
            } else {
                Log.e(
                    SDKApplication.LOG_TAG,
                    "content download setting failed: ${response.errorMessage}"
                )
            }
        }
    }

    fun setLastSeenTrueAndSaveDraftResponse(
        chatroomId: String,
        draftText: String?
    ) {
        val updateLastSeenAndDraftRequest = UpdateLastSeenAndDraftRequest.Builder()
            .chatroomId(chatroomId)
            .draft(draftText)
            .build()
        lmChatClient.updateLastSeenAndDraft(updateLastSeenAndDraftRequest)
        markChatroomAsRead(chatroomId)
    }

    // follow/unfollow a chatroom
    fun followChatroom(
        chatroomId: String,
        value: Boolean,
        source: String
    ) {
        viewModelScope.launchIO {
            // create request
            val request = FollowChatroomRequest.Builder()
                .chatroomId(chatroomId)
                .uuid(userPreferences.getUUID())
                .value(value)
                .build()

            // call api
            val response = lmChatClient.followChatroom(request)
            if (response.success) {
                // Update the ChatRoom actions once CM joins the chatroom
                if (value && getChatroom()?.isSecret == true) {
                    fetchChatroomFromNetwork()
                }
                if (value) {
                    sendChatroomFollowed(source)
                } else {
                    sendChatroomUnfollowed(source)
                }

                val oldChatroomViewData = chatroomDetail.chatroom
                if (oldChatroomViewData != null) {

                    //change follow status for chatroom
                    val followStatus = oldChatroomViewData.followStatus
                    val newChatroomViewData = oldChatroomViewData.toBuilder()
                        .followStatus(!followStatus)
                        .build()

                    chatroomDetail =
                        chatroomDetail.toBuilder().chatroom(newChatroomViewData).build()
                    _chatroomDetailLiveData.postValue(chatroomDetail)
                }
            } else {
                // api failed send error message to ui
                val errorMessage = response.errorMessage
                Log.e(
                    SDKApplication.LOG_TAG,
                    "follow chatroom failed, $errorMessage"
                )
                errorEventChannel.send(ErrorMessageEvent.FollowChatroom(errorMessage))
            }
        }
    }

    // mute/un-mute a chatroom
    fun muteChatroom(
        chatroomId: String,
        value: Boolean
    ) {
        viewModelScope.launchIO {
            // create request
            val request = MuteChatroomRequest.Builder()
                .chatroomId(chatroomId)
                .value(value)
                .build()

            // call api
            val response = lmChatClient.muteChatroom(request)
            if (!response.success) {
                // api failed send error message to ui
                val errorMessage = response.errorMessage
                Log.e(
                    SDKApplication.LOG_TAG,
                    "mute chatroom failed, $errorMessage"
                )
                errorEventChannel.send(ErrorMessageEvent.MuteChatroom(errorMessage))
            }
        }
    }

    private fun fetchChatroomFromNetwork() {
        viewModelScope.launchIO {
            val request = GetChatroomActionsRequest.Builder()
                .chatroomId(chatroomDetail.chatroom?.id ?: "")
                .build()

            val getChatroomActionsResponse = lmChatClient.getChatroomActions(request)
            val data = getChatroomActionsResponse.data ?: return@launchIO
            chatroomDetail = chatroomDetail.toBuilder()
                .actions(ViewDataConverter.convertChatroomActions(data.chatroomActions))
                .participantCount(data.participantCount)
                .canAccessSecretChatRoom(data.canAccessSecretChatroom)
                .build()
            _chatroomDetailLiveData.postValue(chatroomDetail)
        }
    }

    private fun markChatroomAsRead(chatroomId: String) {
        viewModelScope.launchIO {
            val request = MarkReadChatroomRequest.Builder()
                .chatroomId(chatroomId)
                .build()

            val response = lmChatClient.markReadChatroom(request)
            if (response.success) {
                Log.d(SDKApplication.LOG_TAG, "mark read chatroom success.")
            } else {
                Log.e(
                    SDKApplication.LOG_TAG,
                    "mark read chatroom failed: ${response.errorMessage}"
                )
            }
        }
    }

    private fun fetchMemberState() {
        viewModelScope.launch {
            val response = lmChatClient.getMemberState()
            if (response.success) {
                //update manager rights and chatroom view as per rights.
                updateManagementRights(response.data)

                //convert Network Model to ViewData Model
                ViewDataConverter.convertMemberState(response.data)?.let {
                    //update member rights as per rights
                    fetchMemberStateResponse(it)
                }
            } else {
                //set default value as true
                _canMemberRespond.postValue(true)
                _canMemberCreatePoll.postValue(true)
                Log.e(TAG, "${response.errorMessage}")
            }
        }
    }

    /**
     * Update member state of the community to update the chatroom views
     */
    private fun updateManagementRights(memberStateResponse: MemberStateResponse?) {
        if (memberStateResponse == null) return

        memberStateResponse.managerRights?.mapNotNull {
            ViewDataConverter.convertManagementRights(it)
        }?.let {
            this.managementRights.clear()
            this.managementRights.addAll(it)
        }
    }

    //update member rights as per rights
    private fun fetchMemberStateResponse(memberStateViewData: MemberStateViewData) {
        this.currentMemberDataFromMemberState = memberStateViewData.memberViewData
        if (!memberStateViewData.memberRights.isNullOrEmpty()) {
            //check for respond in chatroom
            val memberRespondRight = memberStateViewData.memberRights.firstOrNull {
                it.state == MEMBER_RIGHT_RESPOND_IN_ROOM
            }

            //check for create poll
            val createPollRight = memberStateViewData.memberRights.firstOrNull {
                it.state == MEMBER_RIGHT_CREATE_POLL
            }

            //send to LiveData
            _canMemberRespond.postValue(memberRespondRight?.isSelected ?: true)
            _canMemberCreatePoll.postValue(createPollRight?.isSelected ?: true)
        }
    }

    /** Set a conversation as current topic
     * @param chatroomId Id of the chatroom
     * @param conversation conversation object of the selected conversation
     */
    fun setChatroomTopic(
        chatroomId: String,
        conversation: ConversationViewData,
    ) {
        viewModelScope.launchIO {
            val request = SetChatroomTopicRequest.Builder()
                .chatroomId(chatroomId)
                .conversationId(conversation.id)
                .build()
            val response = lmChatClient.setChatroomTopic(request)
            if (response.success) {
                chatroomDetail = chatroomDetail.toBuilder()
                    .chatroom(
                        getChatroom()?.toBuilder()
                            ?.topic(conversation)
                            ?.build()
                    ).build()
                _setTopicResponse.postValue(conversation)
                sendSetChatroomTopicEvent(chatroomId, conversation.id, conversation.answer)
            } else {
                val errorMessage = response.errorMessage
                Log.e(SDKApplication.LOG_TAG, "set chatroom topic api failed: $errorMessage")
                errorEventChannel.send(ErrorMessageEvent.SetChatroomTopic(errorMessage))
            }
        }
    }

    fun leaveChatRoom(chatroomId: String) {
        viewModelScope.launchIO {
            val request = LeaveSecretChatroomRequest.Builder()
                .chatroomId(chatroomId)
                .isSecret(getChatroom()?.isSecret == true)
                .build()

            val response = lmChatClient.leaveSecretChatroom(request)
            if (response.success) {
                _leaveSecretChatroomResponse.postValue(true)
                sendChatRoomLeftEvent()
            } else {
                val errorMessage = response.errorMessage
                Log.e(SDKApplication.LOG_TAG, "secret leave failed: $errorMessage")
                errorEventChannel.send(ErrorMessageEvent.LeaveSecretChatroom(errorMessage))
            }
        }
    }

    fun deleteConversations(conversations: List<ConversationViewData>) {
        viewModelScope.launchIO {
            viewModelScope.launchIO {
                val conversationIds = conversations.map {
                    it.id
                }
                val request = DeleteConversationsRequest.Builder()
                    .conversationIds(conversationIds)
                    .build()

                val response = lmChatClient.deleteConversations(request)
                if (response.success) {
                    postConversationsDeleted(conversations)
                    _deleteConversationsResponse.postValue(response.data?.conversations?.size)
                    sendMessageDeletedEvent(conversations)
                } else {
                    errorEventChannel.send(ErrorMessageEvent.DeleteConversation(response.errorMessage))
                }
            }
        }
    }

    private fun postConversationsDeleted(conversations: List<ConversationViewData>) {
        sendConversationUpdatesToUI(conversations.map {
            it.toBuilder()
                .deletedBy(userPreferences.getMemberId())
                .deletedByMember(currentMemberFromDb)
                .build()
        })
    }

    fun updateChatroomWhileDeletingTopic() {
        chatroomDetail = chatroomDetail.toBuilder()
            .chatroom(
                getChatroom()?.toBuilder()
                    ?.topic(null)
                    ?.build()
            )
            .build()
    }

    fun updateChatRoomFollowStatus(value: Boolean) {
        val newChatroomViewData = getChatroom()?.toBuilder()
            ?.followStatus(value)
            ?.showFollowTelescope(!value)
            ?.build()
        chatroomDetail = chatroomDetail.toBuilder().chatroom(newChatroomViewData).build()
    }

    @SuppressLint("CheckResult")
    fun postConversation(
        context: Context,
        text: String,
        fileUris: List<SingleUriData>?,
        shareLink: String?,
        replyConversationId: String?,
        replyChatRoomId: String?,
        taggedUsers: List<TagViewData>,
        replyChatData: ChatReplyViewData?,
        metadata: JSONObject? = null
    ) {
        viewModelScope.launchIO {
            val chatroomId = chatroomDetail.chatroom?.id ?: return@launchIO
            val communityId = chatroomDetail.chatroom?.communityId
            val conversationCreatedEpoch = System.currentTimeMillis()
            val temporaryId = "-$conversationCreatedEpoch"
            val updatedFileUris = includeAttachmentMetaData(context, fileUris)
            var postConversationRequestBuilder = PostConversationRequest.Builder()
                .chatroomId(chatroomId)
                .text(text)
                .repliedConversationId(replyConversationId)
                .attachmentCount(updatedFileUris?.size)
                .temporaryId(temporaryId)
                .repliedChatroomId(replyChatRoomId)

            if (!shareLink.isNullOrEmpty()) {
                when {
                    Patterns.EMAIL_ADDRESS.matcher(shareLink).matches() -> {
                        postConversationRequestBuilder =
                            postConversationRequestBuilder.ogTags(null).shareLink(null)
                    }

                    _linkOgTags.value?.url == shareLink &&
                            isValidLinkViewData(_linkOgTags.value) && sendLinkPreview -> {
                        postConversationRequestBuilder = postConversationRequestBuilder
                            .ogTags(
                                ViewDataConverter.convertLinkOGTags(_linkOgTags.value)
                            ).shareLink(shareLink)
                    }

                    isURLReachable(shareLink) && sendLinkPreview -> {
                        postConversationRequestBuilder =
                            postConversationRequestBuilder.shareLink(shareLink)
                    }
                }
            }

            if (metadata != null) {
                postConversationRequestBuilder.metadata(metadata)
            }

            val postConversationRequest = postConversationRequestBuilder.build()

            val temporaryConversation = saveTemporaryConversation(
                userPreferences.getUUID(),
                communityId,
                postConversationRequest,
                updatedFileUris,
                conversationCreatedEpoch
            )
            sendPostedConversationsToUI(temporaryConversation)

            val response = lmChatClient.postConversation(postConversationRequest)
            if (response.success) {
                sendLinkPreview = true
                val data = response.data
                onConversationPosted(
                    context,
                    data,
                    updatedFileUris,
                    temporaryConversation,
                    taggedUsers,
                    replyChatData,
                    replyConversationId,
                    replyChatRoomId
                )
            } else {
                errorEventChannel.send(ErrorMessageEvent.PostConversation(response.errorMessage))
            }
        }
    }

    /**
     * Includes attachment's meta data such as dimensions, thumbnails, etc
     * @param context
     * @param files List<SingleUriData>?
     */
    private fun includeAttachmentMetaData(
        context: Context,
        files: List<SingleUriData>?,
    ): List<SingleUriData>? {
        return files?.map {
            when (it.fileType) {
                IMAGE, GIF -> {
                    val dimensions = FileUtil.getImageDimensions(context, it.uri)
                    it.toBuilder()
                        .width(dimensions.first)
                        .height(dimensions.second)
                        .build()
                }

                VIDEO -> {
                    val thumbnailUri = FileUtil.getVideoThumbnailUri(context, it.uri)
                    if (thumbnailUri != null) {
                        it.toBuilder()
                            .thumbnailUri(thumbnailUri)
                            .build()
                    } else {
                        it
                    }
                }

                else -> it
            }
        }
    }

    private fun isValidLinkViewData(linkViewData: LinkOGTagsViewData?): Boolean {
        if (linkViewData == null)
            return false
        return linkViewData.description != null && linkViewData.image != null && linkViewData.title != null
    }

    private suspend fun isURLReachable(link: String?): Boolean {
        return viewModelScope.async(Dispatchers.IO) {
            try {
                val url = URL(link)
                val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.connectTimeout = 5 * 1000
                httpURLConnection.connect()
                httpURLConnection.responseCode == 200
            } catch (exception: Exception) {
                Log.d(TAG, exception.message.toString())
                false
            }
        }.await()
    }

    private fun saveTemporaryConversation(
        uuid: String,
        communityId: String?,
        request: PostConversationRequest,
        fileUris: List<SingleUriData>?,
        conversationCreatedEpoch: Long
    ): ConversationViewData? {
        val conversation = ViewDataConverter.convertConversation(
            uuid,
            communityId,
            request,
            fileUris,
            conversationCreatedEpoch
        )
        val saveConversationRequest = SaveConversationRequest.Builder()
            .conversation(conversation)
            .build()
        lmChatClient.saveTemporaryConversation(saveConversationRequest)
        val replyConversation = if (conversation.replyConversationId != null) {
            val getConversationRequest = GetConversationRequest.Builder()
                .conversationId(conversation.replyConversationId ?: "")
                .build()
            lmChatClient.getConversation(getConversationRequest).data?.conversation
        } else {
            null
        }
        val getMemberRequest = GetMemberRequest.Builder()
            .uuid(uuid)
            .build()
        val member = lmChatClient.getMember(getMemberRequest).data?.member
        val memberViewData = ViewDataConverter.convertMember(member)
        return ViewDataConverter.convertConversation(conversation, memberViewData)
            ?.toBuilder()
            ?.replyConversation(ViewDataConverter.convertConversation(replyConversation))
            ?.build()
    }

    /**
     * Send updates to UI using live data
     * @param conversation conversation object
     */
    private fun sendPostedConversationsToUI(conversation: ConversationViewData?) {
        if (conversation == null) {
            return
        }
        viewModelScope.launchDefault {
            conversationEventChannel.send(ConversationEvent.PostedConversation(conversation))
        }
    }

    private fun onConversationPosted(
        context: Context,
        response: PostConversationResponse?,
        updatedFileUris: List<SingleUriData>?,
        tempConversation: ConversationViewData?,
        taggedUsers: List<TagViewData>,
        replyChatData: ChatReplyViewData?,
        replyConversationId: String?,
        replyChatRoomId: String?
    ) {
        if (response?.conversation != null) {
            var uploadData: Pair<WorkContinuation?, String>? = null
            val requestList = ArrayList<GenericFileRequest>()
            if (!updatedFileUris.isNullOrEmpty()) {
                uploadData = uploadFilesViaWorker(
                    context,
                    response.id!!,
                    updatedFileUris.size
                )
                requestList.addAll(
                    getUploadFileRequestList(
                        context,
                        updatedFileUris,
                        chatroomDetail.chatroom?.id,
                        response.id
                    )
                )
            }
            savePostedConversation(
                requestList,
                response,
                uploadData?.second
            )
            postedConversation(
                taggedUsers,
                tempConversation,
                replyChatData,
                replyConversationId,
                replyChatRoomId
            )
            // Start Uploading Files
            uploadData?.first?.enqueue()
        }
    }

    private fun getUploadFileRequestList(
        context: Context,
        fileUris: List<SingleUriData>,
        chatroomId: String?,
        conversationId: String?,
    ): List<GenericFileRequest> {
        return fileUris.mapIndexed { index, attachment ->
            val attachmentMeta = AttachmentMetaViewData.Builder()
                .duration(attachment.duration)
                .numberOfPage(attachment.pdfPageCount)
                .size(attachment.size)
                .build()
            var builder = GenericFileRequest.Builder()
                .name(attachment.mediaName)
                .fileUri(attachment.uri)
                .fileType(attachment.fileType)
                .width(attachment.width)
                .height(attachment.height)
                .meta(attachmentMeta)
                .index(index)

            val localFilePath = FileUtil.getRealPath(context, attachment.uri).path

            val file = File(localFilePath)
            val serverPath = UploadHelper.getConversationAttachmentFilePath(
                chatroomId,
                conversationId,
                attachment.fileType,
                file
            )

            //Add thumbnail is present
            if (attachment.thumbnailUri != null) {
                val thumbnailUri = attachment.thumbnailUri
                val thumbnailLocalFilePath = FileUtil.getRealPath(context, thumbnailUri).path
                val thumbnailFile = File(thumbnailLocalFilePath)
                val thumbnailAWSFolderPath = UploadHelper.getConversationAttachmentFilePath(
                    chatroomId,
                    conversationId,
                    attachment.fileType,
                    thumbnailFile,
                    isThumbnail = true
                )
                builder = builder.thumbnailUri(thumbnailUri)
                    .thumbnailLocalFilePath(thumbnailLocalFilePath)
                    .thumbnailAWSFolderPath(thumbnailAWSFolderPath)
            }

            builder.localFilePath(localFilePath).awsFolderPath(serverPath).build()
        }
    }

    private fun savePostedConversation(
        requestList: ArrayList<GenericFileRequest>,
        response: PostConversationResponse,
        uploadWorkerUUID: String?
    ) {
        val attachmentList = requestList.map { request ->
            var builder = Attachment.Builder()
                .name(request.name)
                .url(request.fileUri.toString())
                .type(request.fileType)
                .index(request.index)
                .width(request.width)
                .height(request.height)
                .awsFolderPath(request.awsFolderPath)
                .localFilePath(request.localFilePath)
                .meta(
                    AttachmentMeta.Builder()
                        .duration(request.meta?.duration)
                        .numberOfPage(request.meta?.numberOfPage)
                        .size(request.meta?.size)
                        .build()
                )
            if (request.thumbnailUri != null) {
                builder = builder.thumbnailUrl(request.thumbnailUri.toString())
                    .thumbnailAWSFolderPath(request.thumbnailAWSFolderPath)
                    .thumbnailLocalFilePath(request.thumbnailLocalFilePath)
            }
            return@map builder.build()
        }

        //Get widget from widgetMap and add it to updatedConversation
        val widgetId = response.conversation.widgetId
        val widget = response.widgets[widgetId]

        val updatedConversation = response.conversation.toBuilder()
            .uploadWorkerUUID(uploadWorkerUUID)
            .attachments(attachmentList)
            .widget(widget)
            .build()

        // request to save the posted conversation
        val request = SavePostedConversationRequest.Builder()
            .conversation(updatedConversation)
            .isFromNotification(false)
            .build()
        lmChatClient.savePostedConversation(request)
    }

    private fun postedConversation(
        taggedUsers: List<TagViewData>,
        conversationViewData: ConversationViewData?,
        replyChatData: ChatReplyViewData? = null,
        replyConversationId: String? = null,
        replyChatRoomId: String? = null
    ) {
        if (replyChatData != null) {
            sendMessageReplyEvent(
                conversationViewData,
                replyChatData.repliedMemberId,
                replyChatData.repliedMemberState,
                replyConversationId ?: replyChatRoomId,
                replyChatData.type
            )
        }
        sendChatroomResponded(taggedUsers, conversationViewData)
        if (ChatroomUtil.getConversationType(conversationViewData) == VOICE_NOTE) {
            sendVoiceNoteSent(conversationViewData?.id)
        }
    }

    fun createRetryConversationMediaWorker(
        context: Context,
        conversationId: String,
        attachmentCount: Int,
    ) {
        if (conversationId.isEmpty() || attachmentCount <= 0) {
            return
        }
        val uploadData = uploadFilesViaWorker(context, conversationId, attachmentCount)
        val updateConversationUploadWorkerUUIDRequest =
            UpdateConversationUploadWorkerUUIDRequest.Builder()
                .conversationId(conversationId)
                .uuid(uploadData.second)
                .build()
        lmChatClient.updateConversationUploadWorkerUUID(updateConversationUploadWorkerUUIDRequest)
        uploadData.first.enqueue()
    }

    @SuppressLint("CheckResult", "EnqueueWork", "RestrictedApi")
    private fun uploadFilesViaWorker(
        context: Context,
        conversationId: String,
        fileUriCount: Int,
    ): Pair<WorkContinuation, String> {
        val oneTimeWorkRequest =
            ConversationMediaUploadWorker.getInstance(conversationId, fileUriCount)
        val workContinuation = WorkManager.getInstance(context).beginWith(oneTimeWorkRequest)
        return Pair(workContinuation, oneTimeWorkRequest.id.toString())
    }

    fun fetchRepliedConversationOnClick(
        conversation: ConversationViewData,
        repliedConversationId: String,
        oldConversations: List<BaseViewType>,
    ) {
        val data = fetchPaginatedData(
            SCROLL_UP,
            conversation,
            oldConversations,
            repliedConversationId = repliedConversationId
        )
        if (data != null) {
            viewModelScope.launchMain {
                _paginatedData.value = data
            }
        }
    }

    fun postEditedChatroom(text: String, chatroom: ChatroomViewData) {
        viewModelScope.launchIO {
            val request = EditChatroomTitleRequest.Builder()
                .text(text)
                .chatroomId(chatroom.id)
                .build()

            val response = lmChatClient.editChatroomTitle(request)
            if (!response.success) {
                errorEventChannel.send(
                    ErrorMessageEvent.EditChatroomTitle(
                        response.errorMessage
                    )
                )
            }
        }
    }

    fun postEditedConversation(
        text: String,
        shareLink: String?,
        conversation: ConversationViewData,
    ) {
        viewModelScope.launchIO {
            val request = EditConversationRequest.Builder()
                .conversationId(conversation.id)
                .text(text)
                .shareLink(shareLink)
                .build()

            val response = lmChatClient.editConversation(request)
            if (response.success) {
                postedEditedConversation(text, conversation)
                sendMessageEditedEvent(conversation)
            } else {
                errorEventChannel.send(ErrorMessageEvent.EditConversation(response.errorMessage))
            }
        }
    }

    private fun postedEditedConversation(
        text: String,
        conversation: ConversationViewData,
    ) {
        val updatedConversation = conversation.toBuilder()
            .answer(text)
            .isEdited(true)
            .build()
        sendConversationUpdatesToUI(listOf(updatedConversation))
    }


    fun resendFailedConversation(context: Context, conversation: ConversationViewData) {
        postFailedConversation(context, conversation)
    }

    private fun postFailedConversation(context: Context, conversation: ConversationViewData) {
        viewModelScope.launchIO {
            val chatroomId = chatroomDetail.chatroom?.id ?: return@launchIO
            val postConversationRequest = PostConversationRequest.Builder()
                .chatroomId(chatroomId)
                .text(conversation.answer)
                .shareLink(conversation.ogTags?.url)
                .ogTags(ViewDataConverter.convertLinkOGTags(conversation.ogTags))
                .repliedConversationId(conversation.replyConversation?.id)
                .repliedChatroomId(conversation.replyChatroomId)
                .attachmentCount(conversation.attachments?.size)
                .temporaryId(conversation.temporaryId)
                .build()

            val updateTemporaryConversationRequest = UpdateTemporaryConversationRequest.Builder()
                .conversationId(conversation.id)
                .localSavedEpoch(conversation.localCreatedEpoch ?: System.currentTimeMillis())
                .build()
            lmChatClient.updateTemporaryConversation(updateTemporaryConversationRequest)

            val response = lmChatClient.postConversation(postConversationRequest)
            if (response.success) {
                onFailedConversationPosted(
                    context,
                    conversation,
                    response.data
                )
            }
        }
    }

    private fun onFailedConversationPosted(
        context: Context,
        conversation: ConversationViewData,
        response: PostConversationResponse?,
    ) {
        if (response?.conversation != null) {
            var uploadData: Pair<WorkContinuation?, String>? = null
            val requestList = ArrayList<GenericFileRequest>()
            if (!conversation.attachments.isNullOrEmpty()) {
                uploadData = uploadFilesViaWorker(
                    context,
                    response.id!!,
                    conversation.attachmentCount
                )
                requestList.addAll(getUploadFileRequestList(context, conversation))
            }

            postedConversation(
                emptyList(),
                conversation
            )

            // Start Uploading Files
            uploadData?.first?.enqueue()
        }
    }

    private fun getUploadFileRequestList(
        context: Context,
        conversation: ConversationViewData,
    ): List<GenericFileRequest> {
        return conversation.attachments.orEmpty().mapIndexed { index, attachment ->
            var builder = GenericFileRequest.Builder()
                .name(attachment.name ?: attachment.uri.lastPathSegment)
                .fileUri(attachment.uri)
                .fileType(attachment.type)
                .width(attachment.width)
                .height(attachment.height)
                .meta(attachment.meta)
                .index(index)

            val localFilePath = FileUtil.getRealPath(context, attachment.uri).path
            val file = File(localFilePath)
            val serverPath = UploadHelper.getConversationAttachmentFilePath(
                conversation.chatroomId,
                conversation.id,
                attachment.type,
                file
            )

            //Add thumbnail is present
            if (!attachment.thumbnail.isNullOrEmpty()) {
                val thumbnailUri = Uri.parse(attachment.thumbnail)
                val thumbnailLocalFilePath = FileUtil.getRealPath(context, thumbnailUri).path
                val thumbnailFile = File(thumbnailLocalFilePath)
                val thumbnailAWSFolderPath = UploadHelper.getConversationAttachmentFilePath(
                    conversation.chatroomId,
                    conversation.id,
                    attachment.type,
                    thumbnailFile,
                    isThumbnail = true
                )
                builder = builder.thumbnailUri(thumbnailUri)
                    .thumbnailLocalFilePath(thumbnailLocalFilePath)
                    .thumbnailAWSFolderPath(thumbnailAWSFolderPath)
            }

            builder.localFilePath(localFilePath).awsFolderPath(serverPath).build()
        }
    }

    fun deleteFailedConversation(conversationId: String) {
        val deleteConversationPermanentlyRequest = DeleteConversationPermanentlyRequest.Builder()
            .chatroomId(getChatroom()?.id ?: "")
            .conversationId(conversationId)
            .build()
        lmChatClient.deleteConversationPermanently(deleteConversationPermanentlyRequest)
    }

    fun updateChatroomWhileEditingTopic(
        conversation: ConversationViewData,
        text: String,
    ) {
        val updatedConversation = conversation.toBuilder()
            .answer(text)
            .isEdited(true)
            .build()

        chatroomDetail = chatroomDetail.toBuilder()
            .chatroom(
                getChatroom()?.toBuilder()
                    ?.topic(updatedConversation)
                    ?.build()
            )
            .build()
    }

    /**------------------------------------------------------------
     * Polls
    ---------------------------------------------------------------*/

    fun addNewConversationPollOption(conversationId: String, newPollOption: String) {
        viewModelScope.launchIO {
            val request = AddPollOptionRequest.Builder()
                .conversationId(conversationId)
                .poll(
                    Poll.Builder().text(newPollOption).build()
                )
                .build()

            val response = lmChatClient.addPollOption(request)
            if (response.success) {
                val createdPoll = response.data?.poll
                if (createdPoll != null) {
                    val pollViewData = ViewDataConverter.convertPoll(createdPoll)
                    _addOptionResponse.postValue(pollViewData)
                    sendPollOptionAddedEvent(conversationId)
                }
            } else {
                val errorMessage = response.errorMessage
                Log.e(SDKApplication.LOG_TAG, "add poll option failed: $errorMessage")
                errorEventChannel.send(ErrorMessageEvent.AddPollOption(errorMessage))
            }
        }
    }

    fun submitConversationPoll(
        context: Context,
        conversation: ConversationViewData,
        pollViewDataList: List<PollViewData>,
    ) {
        viewModelScope.launchIO {
            val conversationId = conversation.id
            val oldPollListViewData = conversation.pollInfoData?.pollViewDataList
            if (!isNewPollOptionsSelected(
                    oldPollListViewData as MutableList<PollViewData>?,
                    pollViewDataList
                )
            ) {
                this._pollAnswerUpdated.postValue(conversation.pollInfoData)
                return@launchIO
            }

            val allPollItems = pollViewDataList.map {
                ViewDataConverter.convertPoll(it)
            }
            val updatePollItems = allPollItems.filter { it.isSelected == true }

            val chatroomId = conversation.chatroomId ?: return@launchIO

            val request = SubmitPollRequest.Builder()
                .conversationId(conversationId)
                .chatroomId(chatroomId)
                .polls(updatePollItems)
                .build()

            val response = lmChatClient.submitPoll(context, request)
            pollUpdateResponse(
                response,
                conversation.pollInfoData,
                conversation
            )
        }
    }

    private fun isNewPollOptionsSelected(
        oldPollListViewData: MutableList<PollViewData>?,
        pollViewDataList: List<PollViewData>,
    ): Boolean {
        oldPollListViewData?.let {
            val diff = pollViewDataList.zip(oldPollListViewData).filter {
                it.first.isSelected != it.second.isSelected
            }.size
            //if diff is zero, no poll data has been updated & it makes no sense to make the call
            if (diff == 0) return false
        }
        return true
    }

    private fun pollUpdateResponse(
        baseResponse: LMResponse<Nothing>,
        pollInfoData: PollInfoData?,
        conversation: ConversationViewData? = null,
    ) {
        viewModelScope.launchIO {
            val pollData = if (conversation != null && pollInfoData != null) {
                pollInfoData.toBuilder()
                    .pollViewDataList(pollInfoData.pollViewDataList?.take(1)?.map {
                        it.toBuilder()
                            .parentId(conversation.id)
                            .build()
                    })
                    .build()
            } else {
                pollInfoData
            }
            if (baseResponse.success) {
                _pollAnswerUpdated.postValue(pollData)
                sendPollVotedEvent(conversation)
            } else {
                errorEventChannel.send(ErrorMessageEvent.SubmitPoll(baseResponse.errorMessage))
            }
        }
    }

    /**------------------------------------------------------------
     * Link Preview
    ---------------------------------------------------------------*/

    fun linkPreview(text: String) {
        if (text.isEmpty() || !sendLinkPreview) {
            clearLinkPreview()
            return
        }
        val emails = text.getEmailIfExist()
        if (!emails.isNullOrEmpty())
            return
        val link = text.getUrlIfExist()
        if (!link.isNullOrEmpty()) {
            if (previewLink == link) {
                return
            }
            previewLink = link
            previewLinkJob?.cancel()
            previewLinkJob = viewModelScope.launch {
                delay(250)
                decodeUrl(link)
            }
        } else {
            clearLinkPreview()
        }
    }

    fun removeLinkPreview() {
        sendLinkPreview = false
        previewLinkJob?.cancel()
        previewLink = null
        _linkOgTags.postValue(null)
    }

    fun clearLinkPreview() {
        previewLinkJob?.cancel()
        previewLink = null
        _linkOgTags.postValue(null)
    }

    private fun decodeUrl(url: String) {
        viewModelScope.launchIO {
            val decodeUrlRequest = DecodeUrlRequest.Builder()
                .url(url)
                .build()

            val response = lmChatClient.decodeUrl(decodeUrlRequest)
            if (response.success) {
                decodeUrl(response.data)
            } else {
                _linkOgTags.postValue(null)
            }
        }
    }

    private fun decodeUrl(decodeUrlResponse: DecodeUrlResponse?) {
        val ogTags = decodeUrlResponse?.ogTags ?: return
        _linkOgTags.postValue(ViewDataConverter.convertLinkOGTags(ogTags))
    }

    // calls api to check the status of the DM
    fun checkDMStatus(chatroomId: String) {
        viewModelScope.launchIO {
            val request = CheckDMStatusRequest.Builder()
                .requestFrom(DMRequestFrom.CHATROOM)
                .chatroomId(chatroomId)
                .build()

            val response = lmChatClient.checkDMStatus(request)
            if (response.success) {
                val showDM = response.data?.showDM ?: true
                _showDM.postValue(showDM)
            } else {
                _showDM.postValue(true)
            }
        }
    }

    // calls api to send dm request
    fun sendDMRequest(
        chatroomId: String,
        chatRequestState: ChatRequestState,
        isM2CM: Boolean = false
    ) {
        viewModelScope.launchIO {
            val request = SendDMRequest.Builder()
                .chatroomId(chatroomId)
                .text(dmRequestText)
                .chatRequestState(chatRequestState)
                .build()

            val response = lmChatClient.sendDMRequest(request)
            if (response.success) {
                if (isM2CM) {
                    _dmInitiatedForCM.postValue(true)
                }
                updateChatRequestStateLocally(chatRequestState)
            } else {
                errorEventChannel.send(ErrorMessageEvent.SendDMRequest(response.errorMessage))
            }
        }
    }

    // updates the chatroom view data locally
    private fun updateChatRequestStateLocally(chatRequestState: ChatRequestState) {
        val updatedChatroom = getChatroom()
            ?.toBuilder()
            ?.chatRequestState(chatRequestState)
            ?.chatRequestedById(userPreferences.getMemberId())
            ?.build()

        chatroomDetail = chatroomDetail.toBuilder().chatroom(updatedChatroom).build()
        _updatedChatRequestState.postValue(chatRequestState)
    }

    override fun onCleared() {
        previewLinkJob?.cancel()
        compositeDisposable.dispose()
        super.onCleared()
    }

    // calls api to block/unblock member
    fun blockMember(chatroomId: String, status: MemberBlockState) {
        viewModelScope.launchIO {
            val request = BlockMemberRequest.Builder()
                .chatroomId(chatroomId)
                .status(status)
                .build()

            val response = lmChatClient.blockMember(request)

            if (response.success) {
                if (status == MemberBlockState.MEMBER_BLOCKED) {
                    _memberBlocked.postValue(true)
                    updateChatRequestStateLocally(ChatRequestState.REJECTED)
                } else {
                    _memberBlocked.postValue(false)
                    updateChatRequestStateLocally(ChatRequestState.ACCEPTED)
                }
            } else {
                errorEventChannel.send(ErrorMessageEvent.BlockMember(response.errorMessage))
            }
        }
    }

    /**------------------------------------------------------------
     * Analytics events
    ---------------------------------------------------------------*/

    /**
     * Triggers event when the current user tags someone
     * @param user User object
     * @param communityId Community id
     */
    fun sendUserTagEvent(user: TagViewData, communityId: String?) {
        LMAnalytics.track(
            LMAnalytics.Events.USER_TAGS_SOMEONE,
            mapOf(
                LMAnalytics.Keys.CHATROOM_NAME to getChatroom()?.header,
                "tagged_user_id" to user.id.toString(),
                "tagged_user_name" to user.name,
                LMAnalytics.Keys.COMMUNITY_ID to communityId,
            )
        )
    }

    /**
     * Triggers when the current user opens the chatroom
     * @param extras Chatroom detail fragment extra bundle
     */
    fun sendViewEvent(extras: ChatroomDetailExtras?) {
        if (extras == null) {
            return
        }
        val chatroom = getChatroom()
        LMAnalytics.track(
            LMAnalytics.Events.CHAT_ROOM_OPENED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroom?.id,
                LMAnalytics.Keys.CHATROOM_TYPE to chatroom?.getTypeName(),
                LMAnalytics.Keys.CHATROOM_NAME to chatroom?.header,
                LMAnalytics.Keys.COMMUNITY_ID to chatroom?.communityId,
                LMAnalytics.Keys.SOURCE to extras.source,
                "source_chatroom_id" to extras.sourceChatroomId,
                "source_community_id" to extras.sourceCommunityId,
                "link" to extras.sourceLinkOrRoute,
                "route" to extras.sourceLinkOrRoute,
            )
        )
        if (extras.openedFromLink == true) {
            LMAnalytics.track(
                LMAnalytics.Events.CHATROOM_LINK_CLICKED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom?.id,
                    LMAnalytics.Keys.CHATROOM_TYPE to chatroom?.getTypeName(),
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom?.communityId,
                    LMAnalytics.Keys.SOURCE to extras.source,
                    "source_chatroom_id" to extras.sourceChatroomId,
                    "source_community_id" to extras.sourceCommunityId,
                    "link" to extras.sourceLinkOrRoute
                )
            )
        }
    }

    /**
     * Triggers when the current user un-follows the current chatroom
     * @param source Source of the event
     */
    private fun sendChatroomUnfollowed(source: String) {
        getChatroom()?.let { chatroomViewData ->
            LMAnalytics.track(
                LMAnalytics.Events.CHAT_ROOM_UN_FOLLOWED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroomViewData.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroomViewData.communityId,
                    LMAnalytics.Keys.SOURCE to source,
                )
            )
        }
    }

    /**
     * Triggers when the current user follows the current chatroom
     * @param source Source of the event
     */
    private fun sendChatroomFollowed(source: String) {
        getChatroom()?.let { chatroomViewData ->
            LMAnalytics.track(
                LMAnalytics.Events.CHAT_ROOM_FOLLOWED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroomViewData.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroomViewData.communityId,
                    LMAnalytics.Keys.SOURCE to source,
                    "member_state" to MemberState.getMemberState(
                        chatroomViewData.memberState
                    )
                )
            )
        }
    }

    /**
     * Triggers when the current chatroom is muted or un-muted
     * @param isChatroomMuted Chatroom is muted or not
     */
    fun sendChatroomMuted(isChatroomMuted: Boolean) {
        LMAnalytics.track(
            if (isChatroomMuted) {
                LMAnalytics.Events.CHATROOM_MUTED
            } else {
                LMAnalytics.Events.CHATROOM_UNMUTED
            },
            mapOf(
                LMAnalytics.Keys.CHATROOM_NAME to getChatroom()?.header,
                LMAnalytics.Keys.COMMUNITY_ID to getChatroom()?.communityId
            )
        )
    }

    /**
     * Triggers when the user records a voice message
     **/
    fun sendVoiceNoteRecorded() {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.VOICE_NOTE_RECORDED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.CHATROOM_TYPE to chatroom.type.toString()
                )
            )
        }
    }

    /**
     * Triggers when the user previews a voice message
     **/
    fun sendVoiceNotePreviewed() {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.VOICE_NOTE_PREVIEWED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.CHATROOM_TYPE to chatroom.type.toString()
                )
            )
        }
    }

    /**
     * Triggers when the user removes a recorded voice message
     **/
    fun sendVoiceNoteCanceled() {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.VOICE_NOTE_CANCELED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.CHATROOM_TYPE to chatroom.type.toString()
                )
            )
        }
    }

    /**
     * Triggers when the user sends a voice message
     **/
    private fun sendVoiceNoteSent(conversationId: String?) {
        if (conversationId.isNullOrEmpty()) return
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.VOICE_NOTE_SENT,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.CHATROOM_TYPE to chatroom.type.toString(),
                    LMAnalytics.Keys.MESSAGE_ID to conversationId
                )
            )
        }
    }

    /**
     * Triggers when the user plays a voice message
     **/
    fun sendVoiceNotePlayed(conversationId: String) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.VOICE_NOTE_PLAYED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.CHATROOM_TYPE to chatroom.type.toString(),
                    LMAnalytics.Keys.MESSAGE_ID to conversationId
                )
            )
        }
    }

    /**
     * Triggers when the user plays the audio message
     **/
    fun sendAudioPlayedEvent(messageId: String) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.AUDIO_PLAYED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.MESSAGE_ID to messageId
                )
            )
        }
    }

    /**
     * Triggers when the user clicks on a link
     **/
    fun sendChatLinkClickedEvent(messageId: String?, url: String) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.CHAT_LINK_CLICKED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.UUID to userPreferences.getUUID(),
                    LMAnalytics.Keys.MESSAGE_ID to messageId,
                    "url" to url,
                    "type" to "link",
                )
            )
        }
    }

    /**
     * Triggers when the user deletes messages
     **/
    private fun sendMessageDeletedEvent(conversations: List<ConversationViewData>) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.MESSAGE_DELETED,
                mapOf(
                    "type" to ChatroomUtil.getConversationType(conversations.firstOrNull()),
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    "message_ids" to conversations.joinToString { it.id }
                )
            )
        }
    }

    /**
     * Triggers when the user copy messages
     **/
    fun sendMessageCopyEvent(message: String) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.MESSAGE_COPIED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    "messages" to message
                )
            )
        }
    }

    /**
     * Triggers when topic of chatroom is changed
     */
    private fun sendSetChatroomTopicEvent(
        chatroomId: String,
        conversationId: String,
        topic: String,
    ) {
        LMAnalytics.track(
            LMAnalytics.Events.SET_CHATROOM_TOPIC,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroomId,
                "conversationId" to conversationId,
                "topic" to topic
            )
        )
    }

    /**
     * Triggers when the current user leave the current chatroom
     */
    private fun sendChatRoomLeftEvent() {
        LMAnalytics.track(
            LMAnalytics.Events.CHAT_ROOM_LEFT,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroomDetail.chatroom?.id,
                LMAnalytics.Keys.COMMUNITY_ID to chatroomDetail.chatroom?.communityId,
                LMAnalytics.Keys.CHATROOM_NAME to chatroomDetail.chatroom?.header,
                LMAnalytics.Keys.CHATROOM_TYPE to chatroomDetail.chatroom?.getTypeName(),
                "chatroom_category" to "secret",
            )
        )
    }

    /**
     * Triggers when the current user starts sharing the chatroom
     */
    fun sendChatroomShared() {
        LMAnalytics.track(
            LMAnalytics.Events.CHAT_ROOM_SHARED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to getChatroom()?.id,
                LMAnalytics.Keys.COMMUNITY_ID to getChatroom()?.communityId,
                LMAnalytics.Keys.CHATROOM_NAME to getChatroom()?.header,
                LMAnalytics.Keys.COMMUNITY_NAME to getChatroom()?.communityName,
            )
        )
    }

    /**
     * Triggers when the user edits a message
     **/
    private fun sendMessageEditedEvent(conversation: ConversationViewData?) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.MESSAGE_EDITED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.MESSAGE_ID to conversation?.id
                )
            )
        }
    }

    /**
     * Triggers when the user replies to message
     **/
    private fun sendMessageReplyEvent(
        conversation: ConversationViewData?,
        repliedMemberId: String?,
        repliedMemberState: String?,
        repliedMessageId: String?,
        type: String?
    ) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.MESSAGE_REPLY,
                mapOf(
                    "message_type" to ChatroomUtil.getConversationType(conversation),
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    "replied_to_member_id" to repliedMemberId,
                    "replied_to_member_state" to repliedMemberState,
                    "replied_to_message_id" to repliedMessageId,
                    "type" to type
                )
            )
        }
    }

    /**
     * Triggers when the current user sends a conversation in the current chatroom
     * @param taggedUsers List of all the tagged users present in the sent conversation
     * @param conversation Conversation object
     */
    private fun sendChatroomResponded(
        taggedUsers: List<TagViewData>,
        conversation: ConversationViewData?,
    ) {
        if (conversation == null) {
            return
        }
        LMAnalytics.track(
            LMAnalytics.Events.CHATROOM_RESPONDED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_NAME to getChatroom()?.header,
                LMAnalytics.Keys.CHATROOM_TYPE to getChatroom()?.getTypeName(),
                LMAnalytics.Keys.COMMUNITY_ID to getChatroom()?.communityId,
                LMAnalytics.Keys.SOURCE to "chatroom",
                "message_type" to ChatroomUtil.getConversationType(conversation),
                "count_tagged_user" to taggedUsers.size.toString(),
                "name_tagged_user" to taggedUsers.joinToString { it.name },
                "member_state" to MemberState.getMemberState(currentMemberDataFromMemberState?.state),
            )
        )
    }

    /**
     * Triggers when the current user opens the emoji keyboard view
     */
    fun sendReactionsClickEvent() {
        getChatroomViewData()?.id?.let { chatroomId ->
            LMAnalytics.track(
                LMAnalytics.Events.REACTIONS_CLICKED,
                mapOf(
                    LMAnalytics.Keys.CHATROOM_ID to chatroomId,
                    LMAnalytics.Keys.SOURCE to "chatroom"
                )
            )
        }
    }

    /**
     * Triggers when the current user votes on a chatroom poll or a conversation poll (micro polls)
     */
    private fun sendPollVotedEvent(conversation: ConversationViewData? = null) {
        val chatroom = getChatroomViewData() ?: return
        LMAnalytics.track(
            LMAnalytics.Events.POLL_VOTED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                "member_state" to MemberState.getMemberState(currentMemberDataFromMemberState?.state),
                "chatroom_title" to chatroom.header,
                LMAnalytics.Keys.COMMUNITY_NAME to chatroom.communityName,
                "conversation_id" to conversation?.id
            )
        )
    }

    /**
     * Triggers when the current user creates a chatroom poll option or a conversation poll (micro polls) option
     */
    private fun sendPollOptionAddedEvent(conversationId: String? = null) {
        val chatroom = getChatroomViewData() ?: return
        LMAnalytics.track(
            LMAnalytics.Events.POLL_OPTION_CREATED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                "chatroom_title" to chatroom.header,
                LMAnalytics.Keys.COMMUNITY_NAME to chatroom.communityName,
                "conversation_id" to conversationId
            )
        )
    }

    /**
     * Triggers when the current user edits a chatroom poll or a conversation poll (micro polls)
     */
    fun sendPollVotingEditedEvent(conversationId: String? = null) {
        val chatroom = getChatroomViewData() ?: return
        LMAnalytics.track(
            LMAnalytics.Events.POLL_VOTING_EDITED,
            mapOf(
                LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                "chatroom_title" to chatroom.header,
                LMAnalytics.Keys.COMMUNITY_NAME to chatroom.communityName,
                "conversation_id" to conversationId
            )
        )
    }

    /**
     * Triggers when a user taps on “You and x others voted“ on a micro poll
     **/
    fun sendMicroPollResultsViewed(id: String?) {
        getChatroomViewData()?.let { chatroom ->
            LMAnalytics.track(
                LMAnalytics.Events.POLL_RESULT_VIEWED,
                mapOf(
                    "conversation_id" to id,
                    LMAnalytics.Keys.CHATROOM_ID to chatroom.id,
                    "chatroom_title" to chatroom.header,
                    LMAnalytics.Keys.COMMUNITY_ID to chatroom.communityId,
                    LMAnalytics.Keys.COMMUNITY_NAME to chatroom.communityName
                )
            )
        }
    }
}