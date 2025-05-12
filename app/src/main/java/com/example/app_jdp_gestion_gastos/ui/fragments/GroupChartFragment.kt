package com.example.app_jdp_gestion_gastos.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.app_jdp_gestion_gastos.data.repository.GroupChartRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentGroupChartBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupChartViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupChartViewModelFactory
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.*

class GroupChartFragment : Fragment() {

    private var _binding: FragmentGroupChartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupChartViewModel by viewModels {
        GroupChartViewModelFactory(GroupChartRepository())
    }

    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(selectedMonth)
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMonth = position
                viewModel.loadData(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).toList()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(years.indexOf(selectedYear))
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedYear = years[position]
                viewModel.loadData(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        viewModel.loadData(selectedMonth, selectedYear)

        viewModel.barChartData.observe(viewLifecycleOwner) { (ingresos, gastos) ->
            Log.d("GroupChartFragment", "Ingresos: $ingresos, Gastos: $gastos")
            drawBarChart(ingresos, gastos)
        }
    }

    private fun drawBarChart(ingresos: Float, gastos: Float) {
        // Verifica que los valores de ingresos y gastos sean positivos
        if (ingresos <= 0f && gastos <= 0f) {
            Log.d("GroupChartFragment", "No hay datos para mostrar en el gráfico.")
            return
        }

        val entries = listOf(
            BarEntry(0f, ingresos),
            BarEntry(1f, gastos)
        )

        val barDataSet = BarDataSet(entries, "Comparativa Ingresos vs Gastos").apply {
            colors = listOf(Color.GREEN, Color.RED)
            valueTextSize = 16f
        }

        binding.barChart.apply {
            data = BarData(barDataSet)
            description.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Ingresos", "Gastos"))
            xAxis.granularity = 1f
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}