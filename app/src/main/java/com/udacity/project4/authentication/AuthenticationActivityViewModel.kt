package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthenticationActivityViewModel: ViewModel(){
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    private val _authenticationState = FirebaseUserLiveData().map { user->
        if (user!=null){
            AuthenticationState.AUTHENTICATED
        }else{
            AuthenticationState.UNAUTHENTICATED
        }
    }

    val authenticationState: LiveData<AuthenticationActivityViewModel.AuthenticationState>
        get() = _authenticationState
}