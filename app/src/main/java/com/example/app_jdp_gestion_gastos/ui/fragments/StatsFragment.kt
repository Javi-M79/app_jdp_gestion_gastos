package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.app_jdp_gestion_gastos.data.repository.StatsRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentStatsBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.StatsViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.StatsViewModelFactory

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

        //Mostrar total disponible
        statsViewModel.promedioGastos.observe(viewLifecycleOwner, Observer { promedioGastos ->
            val numeroFormateado = if (promedioGastos < 0) {
                "-%.2f €".format(-promedioGastos)
            } else {
                "%.2f €".format(promedioGastos)
            }

            val textoBase = "Total disponible: "
            val textoCompleto = textoBase + numeroFormateado

            val spannable = SpannableString(textoCompleto)

            // Definir el color según positivo o negativo
            val colorRes =
                if (promedioGastos < 0) android.R.color.holo_red_dark else android.R.color.holo_green_dark
            val color = ContextCompat.getColor(requireContext(), colorRes)

            // Aplicar el color solo al número
            spannable.setSpan(
                ForegroundColorSpan(color),
                textoBase.length, // inicio del número
                textoCompleto.length, // fin del número
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.tvPromedioGastos.text = spannable
        })


        // Observar el número de transacciones de ingresos
        statsViewModel.numTransaccionesIngresos.observe(
            viewLifecycleOwner,
            Observer { numTransaccionesIngresos ->
                binding.tvNumTransaccionesIngresos.text =
                    "Número de Ingresos: $numTransaccionesIngresos"
            })

        // Observar el número de transacciones de gastos
        statsViewModel.numTransaccionesGastos.observe(
            viewLifecycleOwner,
            Observer { numTransaccionesGastos ->
                binding.tvNumTransaccionesGastos.text = "Número de Gastos: $numTransaccionesGastos"
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}