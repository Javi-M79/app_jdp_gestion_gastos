package com.example.app_jdp_gestion_gastos.ui.fragments

import TransactionAdapter
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Transaction
import com.example.app_jdp_gestion_gastos.databinding.FragmentTransactionsBinding
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.util.*

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val calendar = Calendar.getInstance()

    // Lista mutable de transacciones
    private val transactionsList = mutableListOf<Transaction>()

    // Adaptador
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        transactionAdapter = TransactionAdapter(transactionsList)
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = transactionAdapter

        loadTransactions() // Cargamos las transacciones guardadas

        // Habilitar/deshabilitar el botón de eliminar dependiendo de si hay transacciones
        binding.btnDeleteTransaction.isEnabled = transactionsList.isNotEmpty()

        // Configurar DatePicker
        binding.etDate.setOnClickListener { showDatePickerDialog() }

        // Lógica para añadir la transacción
        binding.btnAddTransaction.setOnClickListener { addTransaction() }

        // Lógica para eliminar la transacción seleccionada
        binding.btnDeleteTransaction.setOnClickListener { deleteSelectedTransaction() }

        // Habilitar o deshabilitar el botón de eliminar dependiendo de la selección
        transactionAdapter.setOnSelectionChangedListener { isSelected ->
            binding.btnDeleteTransaction.isEnabled = isSelected
        }
    }
    private fun showDatePickerDialog() {
        DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                binding.etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addTransaction() {
        val description = binding.etDescription.text.toString()
        val amountString = binding.etAmount.text.toString()
        val date = binding.etDate.text.toString()

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

                transactionsList.add(newTransaction)
                transactionAdapter.notifyItemInserted(transactionsList.size - 1)

                saveTransactions() // Guardamos las transacciones en SharedPreferences

                Toast.makeText(requireContext(), "Transacción añadida", Toast.LENGTH_SHORT).show()

                binding.etDescription.text?.clear()
                binding.etAmount.text?.clear()
                binding.etDate.text?.clear()

                // Habilitar el botón de eliminar si la lista no está vacía
                binding.btnDeleteTransaction.isEnabled = transactionsList.isNotEmpty()

            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }    private fun saveTransactions() {
        try {
            val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("transactions_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(transactionsList)
            editor.putString("transactions", json)
            editor.apply()
            Log.d("TransactionsFragment", "Transacciones guardadas correctamente")
        } catch (e: Exception) {
            Log.e("TransactionsFragment", "Error al guardar transacciones: ${e.message}")
        }
    }

    private fun loadTransactions() {
        try {
            val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("transactions_prefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("transactions", null)
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            val savedTransactions: MutableList<Transaction>? = gson.fromJson(json, type)

            if (!savedTransactions.isNullOrEmpty()) {
                transactionsList.clear()
                transactionsList.addAll(savedTransactions)
                transactionAdapter.notifyDataSetChanged()
                Log.d("TransactionsFragment", "Transacciones cargadas correctamente")
            } else {
                Log.d("TransactionsFragment", "No hay transacciones guardadas")
            }

            // Habilitar/deshabilitar el botón de eliminar basado en si la lista está vacía
            binding.btnDeleteTransaction.isEnabled = transactionsList.isNotEmpty()

        } catch (e: Exception) {
            Log.e("TransactionsFragment", "Error al cargar transacciones: ${e.message}")
        }
    }

    private fun deleteSelectedTransaction() {
        val selectedPosition = transactionAdapter.getSelectedPosition()

        if (selectedPosition != RecyclerView.NO_POSITION) {
            // Primero, eliminamos la transacción de la lista
            val removedTransaction = transactionsList[selectedPosition]

            // Eliminar transacción de la lista
            transactionsList.removeAt(selectedPosition)

            // Notificar al adaptador de la eliminación de la transacción
            transactionAdapter.notifyItemRemoved(selectedPosition)

            // Después, actualizamos las transacciones guardadas
            saveTransactions()

            // Habilitar/deshabilitar el botón de eliminar basado en si la lista está vacía
            binding.btnDeleteTransaction.isEnabled = transactionsList.isNotEmpty()

            Toast.makeText(requireContext(), "Transacción eliminada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Por favor, selecciona una transacción", Toast.LENGTH_SHORT).show()
        }
    }    override fun onDestroyView() {
        super.onDestroyView()
        saveTransactions()
        _binding = null
    }
}