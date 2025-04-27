package com.example.app_jdp_gestion_gastos.ui.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.adapter.ExpenseAdapter
import com.example.app_jdp_gestion_gastos.adapter.IncomeAdapter
import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.example.app_jdp_gestion_gastos.data.repository.TransactionsRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentTransactionsBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.EditTransactionDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodel.TransactionsViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.TransactionsViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var expenseAdapter: ExpenseAdapter
    private var selectedDate: Date? = Date()
    private var selectedTransactionId: String? = null

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

        setupAdapters()
        setupUI()
        observeViewModel()

        fetchData()
    }

    private fun setupAdapters() {
        incomeAdapter = IncomeAdapter(
            onTransactionSelected = { income -> openEditDialog(income, isIncome = true) },
            onRequestDelete = { income -> showDeleteIncomeDialog(income) }
        )

        expenseAdapter = ExpenseAdapter(
            onTransactionSelected = { expense -> openEditDialog(expense, isIncome = false) },
            onRequestDelete = { expense -> showDeleteExpenseDialog(expense) }
        )
    }

    private fun setupUI() {
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = incomeAdapter // Default

        binding.btnAddTransaction.setOnClickListener {
            val transactionType = binding.spinnerTransactionType.selectedItem.toString().lowercase()
            if (transactionType == "ingreso") saveIncomeToFirebase()
            else saveExpenseToFirebase()
        }

        binding.etDate.setOnClickListener { showDatePickerDialog() }

        binding.spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateAdapterAndFetch()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setFragmentResultListener("transaction_updated") { _, _ -> fetchData() }
    }

    private fun observeViewModel() {
        transactionsViewModel.incomes.observe(viewLifecycleOwner) { incomes ->
            if (binding.spinnerTransactionType.selectedItem.toString().lowercase() == "ingreso") {
                incomeAdapter.submitList(incomes)
            }
        }

        transactionsViewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            if (binding.spinnerTransactionType.selectedItem.toString().lowercase() == "gasto") {
                expenseAdapter.submitList(expenses)
            }
        }
    }

    private fun fetchData() {
        transactionsViewModel.fetchIncomes()
        transactionsViewModel.fetchExpenses()
    }

    private fun updateAdapterAndFetch() {
        val type = binding.spinnerTransactionType.selectedItem.toString().lowercase()
        if (type == "ingreso") {
            binding.rvTransactions.adapter = incomeAdapter
            transactionsViewModel.fetchIncomes()
        } else {
            binding.rvTransactions.adapter = expenseAdapter
            transactionsViewModel.fetchExpenses()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
            val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate!!)
            binding.etDate.setText(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun openEditDialog(transaction: Any, isIncome: Boolean) {
        val dialog = when (transaction) {
            is Income -> EditTransactionDialog.newInstance(
                transactionId = transaction.id ?: "",
                name = transaction.name,
                amount = transaction.amount,
                category = transaction.category,
                isRecurring = transaction.isRecurring,
                recurrence = transaction.recurrence,
                date = transaction.date?.toDate()?.time ?: System.currentTimeMillis(),
                isIncome = true
            )
            is Expense -> EditTransactionDialog.newInstance(
                transactionId = transaction.id ?: "",
                name = transaction.name,
                amount = transaction.amount,
                category = transaction.category,
                isRecurring = transaction.isRecurring,
                recurrence = transaction.recurrence,
                date = transaction.date?.toDate()?.time ?: System.currentTimeMillis(),
                isIncome = false
            )
            else -> return
        }
        dialog.show(parentFragmentManager, "EditTransactionDialog")
    }

    private fun saveIncomeToFirebase() {
        val name = binding.etDescription.text.toString()
        val amountText = binding.etAmount.text.toString()

        if (name.isBlank() || amountText.isBlank()) {
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
            category = "Ingresos",
            date = Timestamp(selectedDate ?: Date()),
            isRecurring = false,
            recurrence = "ninguna",
            userId = userId
        )

        transactionsViewModel.addIncome(income) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Ingreso guardado con éxito", Toast.LENGTH_SHORT).show()
                transactionsViewModel.fetchIncomes()
                clearInputFields() // <<<<<< Añadido aquí
            } else {
                Toast.makeText(requireContext(), "Error al guardar ingreso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveExpenseToFirebase() {
        val name = binding.etDescription.text.toString()
        val amountText = binding.etAmount.text.toString()

        if (name.isBlank() || amountText.isBlank()) {
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
            category = "Gastos",
            date = Timestamp(selectedDate ?: Date()),
            isRecurring = false,
            recurrence = "ninguna",
            userId = userId
        )

        transactionsViewModel.addExpense(expense) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Gasto guardado con éxito", Toast.LENGTH_SHORT).show()
                transactionsViewModel.fetchExpenses()
                clearInputFields() // <<<<<< Añadido aquí
            } else {
                Toast.makeText(requireContext(), "Error al guardar gasto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTransaction(transactionId: String) {
        transactionsViewModel.deleteTransaction(transactionId) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Transacción eliminada con éxito", Toast.LENGTH_SHORT).show()
                fetchData()
            } else {
                Toast.makeText(requireContext(), "Error al eliminar la transacción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputFields() {
        binding.etDescription.text?.clear()
        binding.etAmount.text?.clear()
        binding.etDate.text?.clear()
        selectedDate = null
    }

    private fun showDeleteExpenseDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar gasto")
            .setMessage("¿Estás seguro de que deseas eliminar este gasto?")
            .setPositiveButton("Sí") { _, _ -> expense.id?.let { deleteTransaction(it) } }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteIncomeDialog(income: Income) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ingreso")
            .setMessage("¿Estás seguro de que deseas eliminar este ingreso?")
            .setPositiveButton("Sí") { _, _ -> income.id?.let { deleteTransaction(it) } }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}