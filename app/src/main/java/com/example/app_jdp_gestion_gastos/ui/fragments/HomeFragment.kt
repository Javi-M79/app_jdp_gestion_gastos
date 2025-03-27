package com.example.app_jdp_gestion_gastos.ui.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.example.app_jdp_gestion_gastos.databinding.FragmentHomeBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.CalendaryDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodel.CalendaryViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val calendaryViewModel: CalendaryViewModel by viewModels()
    private var selectedDate: String = ""
    private var dialogIsVisible = false

    // Obtener el userId desde Firebase
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "user_id_example"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendaryViewModel.loadAllTransactions(userId)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            loadTransactionsForDate(selectedDate)
        }

        calendaryViewModel.incomeTransactions.observe(viewLifecycleOwner) { incomes ->
            val expenses = calendaryViewModel.expenseTransactions.value ?: emptyList()
            if (selectedDate.isNotEmpty() && incomes.isNotEmpty() && expenses.isNotEmpty()) {
                updateTransactionDialog(selectedDate, incomes, expenses)
            }
        }

        calendaryViewModel.expenseTransactions.observe(viewLifecycleOwner) { expenses ->
            val incomes = calendaryViewModel.incomeTransactions.value ?: emptyList()
            if (selectedDate.isNotEmpty() && expenses.isNotEmpty() && incomes.isNotEmpty()) {
                updateTransactionDialog(selectedDate, incomes, expenses)
            }
        }

        binding.btnTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.etDescription)
        val amountEditText = dialogView.findViewById<EditText>(R.id.etAmount)
        val dateEditText = dialogView.findViewById<EditText>(R.id.etDate)
        val incomeRadioButton = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val expenseRadioButton = dialogView.findViewById<RadioButton>(R.id.rbExpense)

        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                dateEditText.setText(formattedDate)
                selectedDate = formattedDate
                Log.d("Fecha seleccionada", selectedDate)  // Verificar la fecha seleccionada
            }, year, month, day)

            datePicker.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Añadir Registro")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val description = descriptionEditText.text.toString()
                val amountString = amountEditText.text.toString()
                val dateString = dateEditText.text.toString() // Usar la fecha seleccionada desde dateEditText

                if (description.isNotEmpty() && amountString.isNotEmpty() && dateString.isNotEmpty()) {
                    val amount = amountString.toDoubleOrNull()
                    if (amount != null) {
                        // Convertir la fecha seleccionada a Timestamp
                        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val date: Date? = try {
                            dateFormatter.parse(dateString) // Parsear la fecha
                        } catch (e: Exception) {
                            null
                        }

                        if (date != null) {
                            val timestamp = Timestamp(date)

                            // Verificar si es ingreso o gasto
                            if (incomeRadioButton.isChecked) {
                                val income = Income(
                                    userId = userId,
                                    name = description,
                                    amount = amount,
                                    date = timestamp,
                                    category = "Salario",
                                    isRecurring = false,
                                    recurrence = ""
                                )
                                saveIncome(income)
                            } else if (expenseRadioButton.isChecked) {
                                val expense = Expense(
                                    userId = userId,
                                    name = description,
                                    amount = amount,
                                    date = timestamp,
                                    category = "Otros",
                                    isRecurring = false,
                                    recurrence = ""
                                )
                                saveExpense(expense)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Fecha no válida", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateTransactionDialog(date: String, incomes: List<Income>, expenses: List<Expense>) {
        if (isAdded && !dialogIsVisible) {
            dialogIsVisible = true
            val dialog = CalendaryDialog(date, incomes, expenses)

            dialog.setOnDismissListener {
                dialogIsVisible = false
            }

            if (incomes.isNotEmpty() || expenses.isNotEmpty()) {
                dialog.updateData(incomes, expenses)
            }

            dialog.show(parentFragmentManager, "TransactionDialog")
        }
    }

    private fun saveIncome(income: Income) {
        calendaryViewModel.addIncome(income)
    }

    private fun saveExpense(expense: Expense) {
        calendaryViewModel.addExpense(expense)
    }

    private fun loadTransactionsForDate(date: String) {
        calendaryViewModel.clearIncomeTransactions()
        calendaryViewModel.clearExpenseTransactions()
        calendaryViewModel.loadIncomesForDate(date)
        calendaryViewModel.loadExpensesForDate(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}