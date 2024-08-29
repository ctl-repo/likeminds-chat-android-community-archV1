package com.likeminds.chatmm.di.dm

import com.likeminds.chatmm.dm.view.DMFeedFragment
import dagger.Subcomponent

@Subcomponent(modules = [DMViewModelModule::class])
interface DMComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): DMComponent
    }

    fun inject(dmFeedFragment: DMFeedFragment)
}