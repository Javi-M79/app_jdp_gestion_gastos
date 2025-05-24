package com.example.app_jdp_gestion_gastos.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.GroupChartRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentGraphicsChartBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupChartViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupChartViewModelFactory
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
import java.io.FileOutputStream
import java.util.*

class GraphicsFragment : Fragment() {

    private var _binding: FragmentGraphicsChartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GroupChartViewModel by viewModels {
        GroupChartViewModelFactory(GroupChartRepository())
    }

    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    // Variables para modo grupo
    private var isGroupMode = false
    private var currentGroupId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphicsChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupRadioButtons()
        setupSavePdfButton()

        // Observa LiveData para actualizar gráficos
        viewModel.barChartData.observe(viewLifecycleOwner) { (ingresos, gastos) ->
            drawBarChart(ingresos, gastos)
            drawPieChart(ingresos, gastos)
            updateBalanceText(ingresos, gastos)
        }

        viewModel.groupId.observe(viewLifecycleOwner) {
            currentGroupId = it
        }

        loadChartData()
    }

    private fun setupSpinners() {
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(selectedMonth)
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMonth = position
                loadChartData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).toList()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(years.indexOf(selectedYear))
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedYear = years[position]
                loadChartData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val modes = listOf("Individual", "Grupal")
        val modeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, modes)
        binding.spinnergrupo.adapter = modeAdapter
        binding.spinnergrupo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                isGroupMode = (position == 1)
                viewModel.setGroupMode(isGroupMode)
                loadChartData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRadioButtons() {
        binding.graphTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBarChart -> {
                    binding.barChart.visibility = View.VISIBLE
                    binding.pieChart.visibility = View.GONE
                }
                R.id.radioPieChart -> {
                    binding.barChart.visibility = View.GONE
                    binding.pieChart.visibility = View.VISIBLE
                }
            }
        }
        // Default show bar chart
        binding.barChart.visibility = View.VISIBLE
        binding.pieChart.visibility = View.GONE
    }

    private fun setupSavePdfButton() {
        binding.btnSaveChart.setOnClickListener {
            val chartView = if (binding.barChart.visibility == View.VISIBLE) binding.barChart else binding.pieChart

            val bitmap = Bitmap.createBitmap(chartView.width, chartView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            chartView.draw(canvas)

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val outputStream = java.io.ByteArrayOutputStream()
            try {
                document.writeTo(outputStream)
                document.close()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al crear PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                return@setOnClickListener
            }

            val pdfBytes = outputStream.toByteArray()
            val pdfFileName = if (binding.barChart.visibility == View.VISIBLE)
                "grafico_barras_ingresos_gastos.pdf"
            else
                "grafico_pastel_ingresos_gastos.pdf"

            val saved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfToDownloads(pdfFileName, pdfBytes)
            } else {
                savePdfLegacy(pdfFileName, pdfBytes)
            }

            if (saved) {
                Toast.makeText(requireContext(), "PDF guardado en Descargas y listo para compartir", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No se pudo guardar el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadChartData() {
        viewModel.loadData(selectedMonth, selectedYear)
    }

    private fun drawBarChart(ingresos: Float, gastos: Float) {
        val entries = listOf(
            BarEntry(0f, ingresos),
            BarEntry(1f, gastos)
        )
        val barDataSet = BarDataSet(entries, "Ingresos vs Gastos").apply {
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

    private fun drawPieChart(ingresos: Float, gastos: Float) {
        val entries = listOf(
            PieEntry(ingresos, "Ingresos"),
            PieEntry(gastos, "Gastos")
        )
        val pieDataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.GREEN, Color.RED)
            valueTextSize = 16f
        }
        binding.pieChart.apply {
            data = PieData(pieDataSet)
            description.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    private fun updateBalanceText(ingresos: Float, gastos: Float) {
        val balance = ingresos - gastos
        binding.tvBalance.text = "Balance: ${"%.2f".format(balance)} €"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfToDownloads(pdfName: String, pdfBytes: ByteArray): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, pdfName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = requireContext().contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val itemUri = resolver.insert(collection, contentValues) ?: run {
            Log.e("PDF_SAVE", "No se pudo crear URI para guardar el PDF")
            return false
        }

        resolver.openOutputStream(itemUri)?.use { outputStream ->
            outputStream.write(pdfBytes)
        } ?: run {
            Log.e("PDF_SAVE", "No se pudo abrir OutputStream para URI")
            return false
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(itemUri, contentValues, null, null)

        sharePdfUri(itemUri)

        return true
    }

    private fun savePdfLegacy(pdfName: String, pdfBytes: ByteArray): Boolean {
        val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(downloadsPath, pdfName)
        return try {
            val fos = FileOutputStream(pdfFile)
            fos.write(pdfBytes)
            fos.flush()
            fos.close()
            Log.d("PDF_SAVE", "PDF guardado en ruta legacy: ${pdfFile.absolutePath}")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun sharePdfUri(uri: android.net.Uri) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir PDF con..."))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}