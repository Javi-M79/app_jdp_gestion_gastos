package com.example.app_jdp_gestion_gastos.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.util.Log
import android.view.*
import android.widget.*
import android.graphics.Canvas
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.GroupChartRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentGraphicsChartBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupChartViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.GroupChartViewModelFactory
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
import java.io.FileOutputStream
import java.util.*

class GraphicsFragment : Fragment() {

    // ViewBinding para acceder a las vistas del layout
    private var _binding: FragmentGraphicsChartBinding? = null
    private val binding get() = _binding!!

    // ViewModel para manejar la lógica de la gráfica
    private val viewModel: GroupChartViewModel by viewModels {
        GroupChartViewModelFactory(GroupChartRepository())
    }

    // Variables para almacenar el mes y año seleccionados
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphicsChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lista de meses
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        // Adaptador para el Spinner de meses
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(selectedMonth)

        // Listener para cuando seleccionamos un mes de la lista
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMonth = position
                viewModel.loadData(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Lista de años desde el 2020 hasta el año actual
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).toList()
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(years.indexOf(selectedYear))

        // Listener para cuando seleccionamos un año de la lista
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedYear = years[position]
                viewModel.loadData(selectedMonth, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener para cuando seleccionamos un tipo de gráfico
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

        // TODO: Botón para guardar el gráfico como PDF
        binding.btnSaveChart.setOnClickListener {
            val chartView = if (binding.barChart.visibility == View.VISIBLE) binding.barChart else binding.pieChart

            // Convertir el gráfico a Bitmap
            chartView.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(chartView.width, chartView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            chartView.draw(canvas)
            chartView.isDrawingCacheEnabled = false
            chartView.destroyDrawingCache()

            // Crear PDF en memoria del telefono
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            val pdfCanvas = page.canvas
            pdfCanvas.drawBitmap(bitmap, 0f, 0f, null)
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

            // Guarda el PDF dependiendo de la versión de Android.
            val saved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfToDownloads(pdfFileName, pdfBytes)
            } else {
                savePdfLegacy(pdfFileName, pdfBytes)
            }

            // Dependiendo del resultado, muestra un mensaje al usuario
            if (saved) {
                Toast.makeText(requireContext(), "PDF guardado en Descargas y listo para compartir", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "No se pudo guardar el PDF", Toast.LENGTH_SHORT).show()
            }
        }

        // Carga los datos iniciales
        viewModel.loadData(selectedMonth, selectedYear)

        // Observa los cambios en los datos del gráfico
        viewModel.barChartData.observe(viewLifecycleOwner) { (ingresos, gastos) ->
            drawBarChart(ingresos, gastos)
            drawPieChart(ingresos, gastos)
            updateBalanceText(ingresos, gastos)
        }
    }

    // TODO: Dibuja el gráfico de barras
    private fun drawBarChart(ingresos: Float, gastos: Float) {
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

    // TODO: Dibuja el gráfico pastel
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

    // TODO: Mostramos el balance en el Textview
    private fun updateBalanceText(ingresos: Float, gastos: Float) {
        val balance = ingresos - gastos
        binding.tvBalance.text = "Balance: ${String.format("%.2f", balance)} €"
    }

    // TODO: Guarda el PDF en el directorio de Descargas y lo comparte
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

        Log.d("PDF_SAVE", "PDF guardado en URI: $itemUri")

        // Escribe los datos en el PDF
        resolver.openOutputStream(itemUri)?.use { outputStream ->
            outputStream.write(pdfBytes)
        } ?: run {
            Log.e("PDF_SAVE", "No se pudo abrir OutputStream para URI")
            return false
        }

        // Finaliza el guardado
        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(itemUri, contentValues, null, null)

        // Compartir PDF usando URI obtenido
        sharePdfUri(itemUri)

        return true
    }

    // TODO: Guarda el PDF en el directorio de Descargas (versiones anteriores a Android 10)
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

    // TODO: Comparte el PDF  con otras aplicaciones
    private fun sharePdfUri(uri: android.net.Uri) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir PDF con..."))
    }

    // Liberamos la referencia del binding cuando se destruye la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}