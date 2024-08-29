package com.likeminds.chatmm.di.chatroomdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.likeminds.chatmm.chatroom.detail.viewmodel.ChatroomDetailViewModel
import com.likeminds.chatmm.chatroom.detail.viewmodel.ViewParticipantsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ChatroomDetailViewModelModule {

    @Binds
    abstract fun bindChatroomDetailViewModelFactory(factory: ChatroomDetailViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ChatroomDetailViewModelKey(ChatroomDetailViewModel::class)
    abstract fun bindChatroomDetailViewModel(chatroomDetailViewModel: ChatroomDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ChatroomDetailViewModelKey(ViewParticipantsViewModel::class)
    abstract fun bindViewParticipantsViewModel(viewParticipantsViewModel: ViewParticipantsViewModel): ViewModel
}