package com.example.app_jdp_gestion_gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app_jdp_gestion_gastos.data.repository.IncomeRepository

class IncomeViewModelFactory(
    private val incomeRepository: IncomeRepository
): ViewModelProvider.Factory{

    override fun <T: ViewModel> create(modelClass: Class<T>):T{
        //Verificacion de que el viewModel solicitado es el correcto
        if(modelClass.isAssignableFrom(IncomeViewModel::class.java)){
            return IncomeViewModel(incomeRepository) as T
        }
        //Si no es el Viewmodel esperado lanzamos un error
        throw IllegalArgumentException("ViewModel Class desconocido")
    }

}