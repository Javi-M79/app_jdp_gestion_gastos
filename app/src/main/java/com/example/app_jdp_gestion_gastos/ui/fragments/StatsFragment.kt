package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Transaction
import com.example.app_jdp_gestion_gastos.databinding.FragmentStatsBinding

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    // Aquí almacenamos las transacciones (puedes sustituir esto por Firebase o base de datos)
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

        // Simulamos algunas transacciones
        transactionsList.add(Transaction(description = "Compra supermercado", amount = 50.0, date = "12-02-2025", icon =R
            .drawable.supermercado))
        transactionsList.add(Transaction(description = "Cena restaurante", amount = 30.0, date = "13-02-2025", icon =R
            .drawable.restaurante))
        transactionsList.add(Transaction(description = "Gasolina", amount = 20.0, date = "14-02-2025", icon =R
            .drawable.otros))

        // Llamamos a la función para actualizar las estadísticas
        updateStats()
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
        binding.tvTotalGastos.text = "Total de Gastos: $${"%.2f".format(totalGastos)}"
        binding.tvPromedioGastos.text = "Promedio de Gastos: $${"%.2f".format(promedioGastos)}"
        binding.tvNumTransacciones.text = "Número de Transacciones: $numTransacciones"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}