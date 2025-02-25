package com.example.app_jdp_gestion_gastos.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_jdp_gestion_gastos.data.model.Transaction
import com.example.app_jdp_gestion_gastos.databinding.FragmentStatsBinding
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val transactionsList = mutableListOf<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransactions()
        updateStats()
    }

    fun reloadStats() {
        loadTransactions()  // Recargar las transacciones desde SharedPreferences
        updateStats()       // Actualizar las estadísticas
    }

    private fun loadTransactions() {
        try {
            val sharedPreferences = requireActivity().getSharedPreferences("transactions_prefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("transactions", null)
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            val savedTransactions: MutableList<Transaction>? = gson.fromJson(json, type)

            if (savedTransactions != null) {
                transactionsList.clear()
                transactionsList.addAll(savedTransactions)
            }
        } catch (e: Exception) {
            Log.e("StatsFragment", "Error al cargar transacciones: ${e.message}")
        }
    }

    private fun updateStats() {
        val totalGastos = transactionsList.sumByDouble { it.amount }
        val promedioGastos = if (transactionsList.isNotEmpty()) {
            totalGastos / transactionsList.size
        } else {
            0.0
        }
        val numTransacciones = transactionsList.size

        // Actualizamos los TextViews con las estadísticas calculadas
        binding.tvTotalGastos.text = "Total de Gastos: ${"%.2f".format(totalGastos)} €"
        binding.tvPromedioGastos.text = "Promedio de Gastos: ${"%.2f".format(promedioGastos)} €"
        binding.tvNumTransacciones.text = "Número de Transacciones: $numTransacciones"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}