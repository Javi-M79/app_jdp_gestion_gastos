package com.example.app_jdp_gestion_gastos.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.CalendarView
import android.widget.EditText
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
        // Crear un diálogo con campos de entrada para la nueva transacción
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
                            icon = icon
                        )

                        // Añadir la transacción a la lista
                        transactionsList.add(newTransaction)
                        saveTransactions() // Guardamos las transacciones
                        highlightTransactionDays() // Actualizamos el calendario
                        showTransactionsForDate(date) // Mostramos las transacciones para la fecha seleccionada

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

            val message = transactionsForDate.joinToString("\n") { "${it.description}: ${"%.2f".format(it.amount)} €" }
            builder.setMessage(message)
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.show()
        } else {
            Toast.makeText(requireContext(), "No hay transacciones en esta fecha", Toast.LENGTH_SHORT).show()
        }
    }

    private fun highlightTransactionDays() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}