package com.example.app_jdp_gestion_gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository


//Esta clase se encargara de proporcionar instancias de los repositorios al ViewModel.
//Ha dado problemas al instanciarlo desde el ViewModel.

class UserModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel(userRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }


}