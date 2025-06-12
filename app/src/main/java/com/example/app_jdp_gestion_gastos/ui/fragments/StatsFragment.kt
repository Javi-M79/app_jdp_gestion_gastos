package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Group
import com.example.app_jdp_gestion_gastos.data.repository.StatsRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentStatsBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.StatsDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodel.StatsViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.StatsViewModelFactory
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val statsViewModel: StatsViewModel by viewModels {
        StatsViewModelFactory(StatsRepository())
    }

    // Adaptador y lista de grupos (para el spinner)
    private var gruposAdapter: ArrayAdapter<String>? = null
    private var gruposList: List<Group> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Variables para los spinners
        val spinnerMeses = binding.spinnerMonthFilter
        val spinnerTipo = binding.spinnerTipoDatos
        val spinnerGrupo = binding.spinnerGrupo

        // Carga los nombres de los meses
        val meses = resources.getStringArray(R.array.months_array)
        spinnerMeses.adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, meses)

        // Selecciona por defecto el mes actual (enero = 0, diciembre = 11)
        val mesActual = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        spinnerMeses.setSelection(mesActual)

        // Carga los tipos de datos (Personal o grupo)
        val tiposDatos = arrayOf("Personales", "Grupo")
        spinnerTipo.adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, tiposDatos)

        // Inicialmente oculta el spinnerGrupo
        spinnerGrupo.visibility = View.GONE

        // Observa la lista de grupos del ViewModel para llenar spinnerGrupo cuando cambie
        statsViewModel.userGroups.observe(viewLifecycleOwner) { grupos ->
            gruposList = grupos
            val nombresGrupos = grupos.map { it.name }
            gruposAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner, nombresGrupos)
            spinnerGrupo.adapter = gruposAdapter
            // Selecciona el primer grupo si existe y actualiza ViewModel
            if (grupos.isNotEmpty()) {
                spinnerGrupo.setSelection(0)
                statsViewModel.setSelectedGroupId(grupos[0].id)
                recargarDatos(spinnerMeses.selectedItemPosition)
            }
        }

        // Listener del spinner de tipo de datos
        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val esGrupo = position == 1
                spinnerGrupo.visibility = if (esGrupo) View.VISIBLE else View.GONE
                statsViewModel.setGrupoSeleccionado(esGrupo)
                // Si es grupo, carga grupos (userGroups se actualizará y spinner también)
                if (!esGrupo) {
                    // Si cambias a "Personales", borrar grupo seleccionado y recargar
                    statsViewModel.setSelectedGroupId(null)
                    recargarDatos(spinnerMeses.selectedItemPosition)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener del spinner de grupos (cuando esta visible)
        spinnerGrupo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Actualiza el groupId seleccionado en ViewModel según posición del spinner
                if (position in gruposList.indices) {
                    val grupoSeleccionado = gruposList[position]
                    statsViewModel.setSelectedGroupId(grupoSeleccionado.id)
                    recargarDatos(spinnerMeses.selectedItemPosition)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener del spinner de meses
        spinnerMeses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                recargarDatos(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Observadores del ViewModel para mostrar totales e información de estadísticas
        statsViewModel.totalIngresos.observe(viewLifecycleOwner) {
            binding.tvTotalIngresos.text = "Total de Ingresos: ${"%.2f".format(it)} €"
        }

        statsViewModel.totalGastos.observe(viewLifecycleOwner) {
            binding.tvTotalGastos.text = "Total de Gastos: ${"%.2f".format(it)} €"
        }

        statsViewModel.promedioGastos.observe(viewLifecycleOwner) { promedio ->
            val textoBase = "Total disponible: "
            val numeroFormateado = if (promedio < 0) "-%.2f €".format(-promedio) else "%.2f €".format(promedio)
            val textoCompleto = textoBase + numeroFormateado
            val spannable = SpannableString(textoCompleto)
            val colorRes = if (promedio < 0) android.R.color.holo_red_dark else android.R.color.holo_green_dark
            val color = ContextCompat.getColor(requireContext(), colorRes)
            spannable.setSpan(ForegroundColorSpan(color), textoBase.length, textoCompleto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvPromedioGastos.text = spannable
        }

        statsViewModel.numTransaccionesIngresos.observe(viewLifecycleOwner) {
            binding.tvNumTransaccionesIngresos.text = "Número de Ingresos: $it"
        }

        statsViewModel.numTransaccionesGastos.observe(viewLifecycleOwner) {
            binding.tvNumTransaccionesGastos.text = "Número de Gastos: $it"
        }

        binding.tvTotalIngresos.setOnClickListener {
            statsViewModel.getIncomesForMonth { ingresos ->
                showStatsDialog("Ingresos", ingresos)
            }
        }

        binding.tvTotalGastos.setOnClickListener {
            statsViewModel.getExpensesForMonth { gastos ->
                showStatsDialog("Gastos", gastos)
            }
        }
    }

    private fun recargarDatos(posMes: Int) {
        animateViews()
        statsViewModel.loadStatsForMonth(if (posMes == 0) null else posMes - 1)
    }

    private fun animateViews() { /* Implementación opcional */ }

    private fun showStatsDialog(title: String, items: List<String>) {
        StatsDialog.newInstance(title, items).show(childFragmentManager, "statsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}