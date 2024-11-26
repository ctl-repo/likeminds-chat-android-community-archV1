package com.likeminds.chatmm.xapp

import com.likeminds.chatmm.xapp.helper.AuthChecker
import com.likeminds.chatmm.xapp.helper.FinXNavigator

object FinXDependencies {

    lateinit var authChecker: AuthChecker
    lateinit var appFinXNavigator: FinXNavigator

    fun initialize(authChecker: AuthChecker,finXNavigator: FinXNavigator){
        this.authChecker=authChecker
        this.appFinXNavigator=finXNavigator
    }
}