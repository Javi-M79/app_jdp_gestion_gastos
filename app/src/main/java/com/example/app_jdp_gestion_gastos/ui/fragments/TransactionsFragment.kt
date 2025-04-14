package com.example.app_jdp_gestion_gastos.ui.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.launch
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

        incomeAdapter = IncomeAdapter(
            onTransactionSelected = { income ->
                val id = income.id
                if (id.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Error: ingreso sin ID", Toast.LENGTH_SHORT).show()
                    return@IncomeAdapter
                }

                val dialog = EditTransactionDialog.newInstance(
                    transactionId = id,
                    name = income.name,
                    amount = income.amount,
                    category = income.category,
                    isRecurring = income.isRecurring,
                    recurrence = income.recurrence,
                    date = income.date?.toDate()?.time ?: System.currentTimeMillis(),
                    isIncome = true
                )
                dialog.show(parentFragmentManager, "EditIncomeDialog")
            },

            onRequestDelete = { income ->
                showDeleteIncomeDialog(income)
            }
        )



        expenseAdapter = ExpenseAdapter(
            onTransactionSelected = { expense ->
                val id = expense.id
                if (id.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Error: gasto sin ID", Toast.LENGTH_SHORT)
                        .show()
                    return@ExpenseAdapter
                }

                val dialog = EditTransactionDialog.newInstance(
                    transactionId = id,
                    name = expense.name,
                    amount = expense.amount,
                    category = expense.category,
                    isRecurring = expense.isRecurring,
                    recurrence = expense.recurrence,
                    date = expense.date?.toDate()?.time ?: System.currentTimeMillis(),
                    isIncome = false
                )
                dialog.show(parentFragmentManager, "EditExpenseDialog")
            },

            onRequestDelete = { expense ->
                showDeleteExpenseDialog(expense)
            }
        )


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


        setFragmentResultListener("transaction_updated") { _, _ ->
            val tipo = binding.spinnerTransactionType.selectedItem.toString().lowercase()
            if (tipo == "gasto") {
                binding.rvTransactions.adapter = expenseAdapter
                transactionsViewModel.fetchExpenses()
            } else {
                binding.rvTransactions.adapter = incomeAdapter
                transactionsViewModel.fetchIncomes()
            }
        }


        binding.spinnerTransactionType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val transactionType = binding.spinnerTransactionType.selectedItem.toString()
                    if (transactionType == "ingreso") {
                        binding.rvTransactions.adapter = incomeAdapter
                    } else {
                        binding.rvTransactions.adapter = expenseAdapter
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.btnDeleteTransaction.setOnClickListener {
            selectedTransactionId?.let {
                deleteTransaction(it)
            } ?: Toast.makeText(
                requireContext(),
                "Selecciona una transacción para eliminar",
                Toast.LENGTH_SHORT
            ).show()
        }

            transactionsViewModel.incomes.observe(viewLifecycleOwner) { incomes ->
                val type = binding.spinnerTransactionType.selectedItem.toString().lowercase()
                if(type == "ingreso"){
                    binding.rvTransactions.adapter = incomeAdapter
                    incomeAdapter.submitList(incomes.toList())
                    incomeAdapter.notifyDataSetChanged()
                }
            }

            transactionsViewModel.expenses.observe(viewLifecycleOwner) { expenses ->
                val type = binding.spinnerTransactionType.selectedItem.toString().lowercase()
                if (type == "gasto") {
                    binding.rvTransactions.adapter = expenseAdapter
                    expenseAdapter.submitList(expenses.toList())
                    expenseAdapter.notifyDataSetChanged()
                }
            }

        //Recarga los datos si hay actualizaciones
        transactionsViewModel.fetchExpenses()
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

        val datePicker =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = selectedCalendar.time
                val formattedDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate!!)
                binding.etDate.setText(formattedDate)
            }, year, month, day)

        datePicker.show()
    }

    private fun saveIncomeToFirebase() {
        val amountText = binding.etAmount.text.toString()
        val name = binding.etDescription.text.toString()

        if (amountText.isBlank() || name.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Todos los campos son obligatorios",
                Toast.LENGTH_SHORT
            ).show()
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
                Toast.makeText(requireContext(), "Ingreso guardado con éxito", Toast.LENGTH_SHORT)
                    .show()
                transactionsViewModel.fetchIncomes()
            } else {
                Toast.makeText(requireContext(), "Error al guardar ingreso", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun saveExpenseToFirebase() {
        val amountText = binding.etAmount.text.toString()
        val name = binding.etDescription.text.toString()

        if (amountText.isBlank() || name.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Todos los campos son obligatorios",
                Toast.LENGTH_SHORT
            ).show()
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
                Toast.makeText(requireContext(), "Gasto guardado con éxito", Toast.LENGTH_SHORT)
                    .show()
                transactionsViewModel.fetchExpenses()
            } else {
                Toast.makeText(requireContext(), "Error al guardar gasto", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

//DIALOGO PARA ELIMINAR GASTOS
    private fun showDeleteExpenseDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar gasto")
            .setMessage("¿Estás seguro de que deseas eliminar este gasto?")
            .setPositiveButton("Sí") { _, _ ->
                transactionsViewModel.deleteTransaction(expense.id ?: "") { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Gasto eliminado con éxito", Toast.LENGTH_SHORT).show()
                        transactionsViewModel.fetchExpenses()
                    } else {
                        Toast.makeText(requireContext(), "Error al eliminar el gasto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    //DIALOGO PARA ELIMINAR INGRESOS
    private fun showDeleteIncomeDialog(income: Income) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar ingreso")
            .setMessage("¿Estás seguro de que deseas eliminar este ingreso?")
            .setPositiveButton("Sí") { _, _ ->
                transactionsViewModel.deleteTransaction(income.id ?: "") { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Ingreso eliminado con éxito", Toast.LENGTH_SHORT).show()
                        transactionsViewModel.fetchIncomes()
                    } else {
                        Toast.makeText(requireContext(), "Error al eliminar ingreso", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


//ELIMINAR TRANSACCION
    private fun deleteTransaction(transactionId: String) {
        Log.d("TransactionsFragment", "Intentando eliminar transacción con ID: $transactionId")
        transactionsViewModel.deleteTransaction(transactionId) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "Transacción eliminada con éxito",
                    Toast.LENGTH_SHORT
                ).show()
                transactionsViewModel.fetchIncomes()
                transactionsViewModel.fetchExpenses()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al eliminar la transacción",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun onTransactionSelected(transactionId: String) {
        selectedTransactionId = transactionId
        binding.btnDeleteTransaction.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}







