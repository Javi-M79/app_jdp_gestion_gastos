package com.example.app_jdp_gestion_gastos.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Transaction
import com.example.app_jdp_gestion_gastos.databinding.FragmentHomeBinding
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val transactionsList = mutableListOf<Transaction>()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTransactions()

        val calendarView = binding.calendarView

        // Listener para seleccionar fecha
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            showTransactionsForDate(selectedDate)
        }

        // Resaltar los días con transacciones
        highlightTransactionDays()

        // Configurar el FloatingActionButton para agregar una nueva transacción
        binding.btnTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val amountEditText = dialogView.findViewById<EditText>(R.id.etAmount)
        val dateEditText = dialogView.findViewById<EditText>(R.id.etDate)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Añadir Transacción")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val description = descriptionEditText.text.toString()
                val amountString = amountEditText.text.toString()
                val date = dateEditText.text.toString()

                if (description.isNotEmpty() && amountString.isNotEmpty() && date.isNotEmpty()) {
                    try {
                        val amount = amountString.toDouble()
                        val type = if (amount >= 0) "ingreso" else "gasto"

                        val icon = when {
                            description.contains("restaurante", true) -> R.drawable.restaurante
                            description.contains("supermercado", true) -> R.drawable.supermercado
                            else -> R.drawable.otros
                        }

                        val newTransaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            description = description,
                            amount = amount,
                            date = date,
                            type = type,
                            icon = icon
                        )

                        transactionsList.add(newTransaction)
                        saveTransactions()
                        highlightTransactionDays()
                        showTransactionsForDate(date)
                        updateExpenseText()

                        Toast.makeText(requireContext(), "Transacción añadida", Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun loadTransactions() {
        try {
            val sharedPreferences: SharedPreferences =
                requireActivity().getSharedPreferences("transactions_prefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("transactions", null)
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            val savedTransactions: MutableList<Transaction>? = gson.fromJson(json, type)

            if (!savedTransactions.isNullOrEmpty()) {
                transactionsList.clear()
                transactionsList.addAll(savedTransactions)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al cargar transacciones", Toast.LENGTH_SHORT).show()
        }
        updateExpenseText() // Actualiza los gastos y presupuesto
    }

    private fun saveTransactions() {
        try {
            val sharedPreferences: SharedPreferences =
                requireActivity().getSharedPreferences("transactions_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(transactionsList)
            editor.putString("transactions", json)
            editor.apply()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al guardar transacciones", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTransactionsForDate(selectedDate: String) {
        val transactionsForDate = transactionsList.filter { it.date == selectedDate }

        if (transactionsForDate.isNotEmpty()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Transacciones del $selectedDate")

            val spannableBuilder = SpannableStringBuilder()

            transactionsForDate.forEach { transaction ->
                val transactionText = "${transaction.description}: "
                val colorTransaction = "${"%.2f".format(transaction.amount)} € (${transaction.type})\n"

                val color = if (transaction.type == "ingreso")
                    ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                else
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)

                // Agregar la descripción sin color
                spannableBuilder.append(transactionText)

                // Agregar la parte coloreada
                val spannableString = SpannableString(colorTransaction)
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    0,
                    colorTransaction.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableBuilder.append(spannableString)
            }

            builder.setMessage(spannableBuilder)
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } else {
            Toast.makeText(requireContext(), "No hay transacciones en esta fecha", Toast.LENGTH_SHORT).show()
        }
    }    private fun highlightTransactionDays() {
        val calendarView = binding.calendarView

        // Obtener las fechas de las transacciones
        val transactionDates = transactionsList.map { it.date }

        // Convertir las fechas a formato Date (día, mes y año)
        val calendar = Calendar.getInstance()

        transactionDates.forEach { date ->
            val dateParts = date.split("-")
            if (dateParts.size == 3) {
                val day = dateParts[0].toInt()
                val month = dateParts[1].toInt() - 1 // Los meses en Calendar son 0-indexados
                val year = dateParts[2].toInt()

                // Establecer la fecha en el calendario
                calendar.set(year, month, day)

                // Resaltar el día en el calendario
                calendarView.setDate(calendar.timeInMillis, false, true)
                calendarView.setBackgroundColor(Color.YELLOW) // Color de resaltado
            }
        }
    }

    private fun updateExpenseText() {
        // Cargar el presupuesto desde SharedPreferences
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val monthlyBudget = sharedPreferences.getFloat("monthlyBudget", 0f)

        // Calcular el total de los gastos
        val totalExpenses = transactionsList.sumOf { it.amount }

        // Actualizar el TextView con el gasto y el presupuesto
        binding.tvPresupuesto.text = "Gastos: ${"%.2f".format(totalExpenses)} € / Presupuesto: ${"%.2f".format(monthlyBudget)} €"

        // Comprobar si el gasto ha alcanzado el 80% del presupuesto
        if (totalExpenses >= monthlyBudget * 0.8) {
            showBudgetWarningDialog(totalExpenses, monthlyBudget)
        }
    }

    private fun showBudgetWarningDialog(totalExpenses: Double, monthlyBudget: Float) {
        val progress = (totalExpenses / monthlyBudget * 100).toInt()

        // Elegir el color según el porcentaje
        val color = when {
            progress < 60 -> Color.GREEN
            progress in 60..80 -> Color.MAGENTA
            else -> Color.RED
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_budget_warning, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.tvWarningMessage)

        progressBar.max = 100
        progressBar.progress = progress
        textViewMessage.text = "Has alcanzado el $progress% de tu presupuesto mensual.\nGastos actuales: ${"%.2f".format(totalExpenses)} € / Presupuesto: ${"%.2f".format(monthlyBudget)} €"
        textViewMessage.setTextColor(color)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("¡Atención!")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}