package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.example.app_jdp_gestion_gastos.databinding.FragmentStatsBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.StatsViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.StatsViewModelFactory
import com.example.app_jdp_gestion_gastos.data.repository.StatsRepository
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    // Crear instancia del ViewModel utilizando el ViewModelFactory
    private val statsViewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(StatsRepository()) // Pasar el repositorio
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar las estadísticas
        statsViewModel.loadStats()

        // Observar el total de ingresos
        statsViewModel.totalIngresos.observe(viewLifecycleOwner, Observer { totalIngresos ->
            binding.tvTotalIngresos.text = "Total de Ingresos: ${"%.2f".format(totalIngresos)} €"
        })

        // Observar el total de gastos
        statsViewModel.totalGastos.observe(viewLifecycleOwner, Observer { totalGastos ->
            binding.tvTotalGastos.text = "Total de Gastos: ${"%.2f".format(totalGastos)} €"
        })

        // Observar el promedio de gastos
        statsViewModel.promedioGastos.observe(viewLifecycleOwner, Observer { promedioGastos ->
            binding.tvPromedioGastos.text = "Promedio de Gastos: ${"%.2f".format(promedioGastos)} €"
        })

        // Observar el número de transacciones de ingresos
        statsViewModel.numTransaccionesIngresos.observe(viewLifecycleOwner, Observer { numTransaccionesIngresos ->
            binding.tvNumTransaccionesIngresos.text = "Número de Ingresos: $numTransaccionesIngresos"
        })

        // Observar el número de transacciones de gastos
        statsViewModel.numTransaccionesGastos.observe(viewLifecycleOwner, Observer { numTransaccionesGastos ->
            binding.tvNumTransaccionesGastos.text = "Número de Gastos: $numTransaccionesGastos"
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}