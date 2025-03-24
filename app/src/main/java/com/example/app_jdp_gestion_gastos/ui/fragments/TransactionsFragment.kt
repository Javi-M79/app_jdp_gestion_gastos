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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.example.app_jdp_gestion_gastos.data.repository.IncomeRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentTransactionsBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.IncomeViewModel
import com.example.app_jdp_gestion_gastos.adapter.IncomeAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeAdapter: IncomeAdapter

    private val incomeViewModel: IncomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return IncomeViewModel(IncomeRepository()) as T
            }
        }
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

        // Inicializar el RecyclerView y su adaptador
        incomeAdapter = IncomeAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = incomeAdapter

        binding.btnAddTransaction.setOnClickListener {
            saveIncomeToFirebase()
        }

        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val transactionType = binding.spinnerTransactionType.selectedItem.toString()
                if (transactionType == "Ingresos") {
                    saveIncomeToFirebase()
                } else {
                    /*saveExpenseToFirebase() // Agrega una función para manejar gastos*/
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        lifecycleScope.launch {
            incomeViewModel.incomes.collect { incomes ->
                updateIncomeList(incomes)
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            binding.etDate.setText(formattedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun updateIncomeList(incomes: List<Income>) {
        incomeAdapter.submitList(incomes)
    }

    private fun saveIncomeToFirebase() {
        val amountText = binding.etAmount.text.toString()
        val name = binding.etDescription.text.toString()
        val category = "General"

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
            date = Timestamp(Date()),
            isRecurring = false,
            recurrence = "ninguna",
            userId = userId
        )

        incomeViewModel.addIncome(income) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Ingreso guardado con éxito", Toast.LENGTH_SHORT).show()
                incomeViewModel.fetchIncomes(userId) // Actualizar lista de ingresos
            } else {
                Toast.makeText(requireContext(), "Error al guardar ingreso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}