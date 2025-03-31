package com.example.app_jdp_gestion_gastos.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.example.app_jdp_gestion_gastos.data.repository.TransactionsRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentTransactionsBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.TransactionsViewModel
import com.example.app_jdp_gestion_gastos.adapter.ExpenseAdapter
import com.example.app_jdp_gestion_gastos.adapter.IncomeAdapter
import com.example.app_jdp_gestion_gastos.ui.viewmodel.TransactionsViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var expenseAdapter: ExpenseAdapter
    private var selectedDate: Date? = Date()

    // Utilizando ViewModel con Factory
    private val transactionsViewModel: TransactionsViewModel by viewModels {
        TransactionsViewModelFactory(TransactionsRepository())
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeAdapter = IncomeAdapter()
        expenseAdapter = ExpenseAdapter()

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = incomeAdapter

        binding.btnAddTransaction.setOnClickListener {
            val transactionType = binding.spinnerTransactionType.selectedItem.toString()
            if (transactionType == "ingreso") {
                saveIncomeToFirebase()
            } else {
                saveExpenseToFirebase()
            }
        }

        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val transactionType = binding.spinnerTransactionType.selectedItem.toString()
                if (transactionType == "ingreso") {
                    binding.rvTransactions.adapter = incomeAdapter
                } else {
                    binding.rvTransactions.adapter = expenseAdapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lifecycleScope.launch {
            transactionsViewModel.incomes.collect { incomes ->
                transactionsViewModel.updateIncomeList(incomes)
                incomeAdapter.submitList(incomes)
            }
        }

        lifecycleScope.launch {
            transactionsViewModel.expenses.collect { expenses ->
                transactionsViewModel.updateExpenseList(expenses)
                expenseAdapter.submitList(expenses)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        transactionsViewModel.fetchIncomes()
        transactionsViewModel.fetchExpenses()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            selectedDate = selectedCalendar.time
            val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate!!)
            binding.etDate.setText(formattedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun saveIncomeToFirebase() {
        val amountText = binding.etAmount.text.toString()
        val name = binding.etDescription.text.toString()
        val category = "Ingresos"

        if (amountText.isBlank() || name.isBlank()) {
            Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: "usuario_demo"

        val income = Income(
            amount = amount,
            name = name,
            category = category,
            date = Timestamp(selectedDate ?: Date()),
            isRecurring = false,
            recurrence = "ninguna",
            userId = userId
        )

        transactionsViewModel.addIncome(income) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Ingreso guardado con éxito", Toast.LENGTH_SHORT).show()
                transactionsViewModel.fetchIncomes()
            } else {
                Toast.makeText(requireContext(), "Error al guardar ingreso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveExpenseToFirebase() {
        val amountText = binding.etAmount.text.toString()
        val name = binding.etDescription.text.toString()
        val category = "Gastos"

        if (amountText.isBlank() || name.isBlank()) {
            Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: "usuario_demo"

        val expense = Expense(
            amount = amount,
            name = name,
            category = category,
            date = Timestamp(selectedDate ?: Date()),
            isRecurring = false,
            recurrence = "ninguna",
            userId = userId
        )

        transactionsViewModel.addExpense(expense) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Gasto guardado con éxito", Toast.LENGTH_SHORT).show()
                transactionsViewModel.fetchExpenses()
            } else {
                Toast.makeText(requireContext(), "Error al guardar gasto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}