package com.example.app_jdp_gestion_gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.example.app_jdp_gestion_gastos.data.repository.IncomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IncomeViewModel(private val incomeRepository: IncomeRepository) : ViewModel() {

    private val _incomes = MutableStateFlow<List<Income>>(emptyList())
    val incomes: StateFlow<List<Income>> get() = _incomes

    fun addIncome(income: Income, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = incomeRepository.addIncome(income)
            onResult(success)
            if (success) fetchIncomes(income.userId)
        }
    }

    //Modificar ingreso
    // TODO




    fun fetchIncomes(userId: String) {
        viewModelScope.launch {
            val incomesList = incomeRepository.getIncomesByUser(userId)
            _incomes.value = incomesList // Asegurar que los datos se actualizan
        }
    }
}