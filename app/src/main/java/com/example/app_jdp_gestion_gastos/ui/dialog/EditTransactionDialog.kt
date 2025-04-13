package com.example.app_jdp_gestion_gastos.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.app_jdp_gestion_gastos.data.repository.ExpenseRepository
import com.example.app_jdp_gestion_gastos.data.repository.IncomeRepository
import com.example.app_jdp_gestion_gastos.data.repository.TransactionsRepository
import com.example.app_jdp_gestion_gastos.databinding.DialogEditTransactionBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.ExpenseViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.ExpenseViewModelFactory
import com.example.app_jdp_gestion_gastos.ui.viewmodel.IncomeViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.IncomeViewModelFactory
import com.example.app_jdp_gestion_gastos.ui.viewmodel.TransactionsViewModel
import com.example.app_jdp_gestion_gastos.ui.viewmodel.TransactionsViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTransactionDialog : DialogFragment() {

    private var _binding: DialogEditTransactionBinding? = null
    private val binding get() = _binding!!


    //Crear instancia del dialogo con datos editables.
    companion object {
        fun newInstance(
            transactionId: String,
            name: String,
            amount: Double,
            category: String,
            isRecurring: Boolean,
            recurrence: String,
            date: Long, //TODO REVISAR PARA VER SI LO CARGA BIEN EN FIREBASE
            isIncome: Boolean
        ): EditTransactionDialog {
            val args = Bundle().apply {
                putString("transactionId", transactionId)
                putString("name", name)
                putDouble("amount", amount)
                putString("category", category)
                putBoolean("isRecurring", isRecurring)
                putString("recurrence", recurrence)
                putBoolean("isIncome", isIncome)
                putLong("dateMillis", date)
            }

            val fragment = EditTransactionDialog()
            fragment.arguments = args
            return fragment
        }
    }

    //Cargar el layout y devolver el dialogo

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditTransactionBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        setupUI()
        return dialog
    }


    //Mostrar datos en los campos
    private fun setupUI() {
        val args = requireArguments()
        val recurrenceOptions = listOf("ninguna", "diaria", "semanal", "mensual")
        val recurrenceAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrenceOptions)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRecurrence.adapter = recurrenceAdapter

        // Rellenar los campos con datos existentes
        binding.etName.setText(args.getString("name", ""))
        binding.etCategory.setText(args.getString("category", ""))
        binding.etAmount.setText(args.getDouble("amount", 0.0).toString())
        binding.switchRecurring.isChecked = args.getBoolean("isRecurring", false)

        val recurrence = args.getString("recurrence", "ninguna")
        binding.spinnerRecurrence.setSelection(recurrenceOptions.indexOf(recurrence))

        val date = Date(args.getLong("dateMillis"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(date))

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = date }
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val picked = Calendar.getInstance()
                    picked.set(y, m, d)
                    binding.etDate.setText(dateFormat.format(picked.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        //Logica boton guardar
        binding.btnSave.setOnClickListener {

            val args = requireArguments()

            //Datos recogidos de firestore
            val originalName = args.getString("name") ?: ""
            val originalCategory = args.getString("category") ?: ""
            val originalAmount = args.getDouble("amount")
            val originalIsRecurring = args.getBoolean("isRecurring")
            val originalRecurrence = args.getString("recurrence") ?: ""
            val originalDateMillis = args.getLong("dateMillis")

            // Nuevos campos
            val updatedFields = mutableMapOf<String, Any>()


            //Modificacion del nombre.
            val newName = binding.etName.text.toString()
            // Si el nuevo nombre es diferente al original actualizamos el campo al nuevo nombre.
            if (newName != originalName) updatedFields["name"] = newName

            //Modificacion de categoria
            val newCategory = binding.etCategory.text.toString()
            // Si la nueva categoria es diferente al original actualizamos el campo a la nueva categoria.
            if (newCategory != originalCategory) updatedFields["category"] = newCategory


            // Modificacion del montante
            val newAmount = binding.etAmount.text.toString().toDoubleOrNull()
            //// Si el nuevo montante es diferente al original y no es nulo actualizamos el campo al nuevo montante
            if (newAmount != null && newAmount != originalAmount) {
                updatedFields["amount"] = newAmount
            }

            //Modificacion de la recurrencia
            val newIsRecurring = binding.switchRecurring.isChecked
            if (newIsRecurring != originalIsRecurring) updatedFields["isRecurring"] = newIsRecurring

            //Nueva recurrencia
            val newRecurrence = binding.spinnerRecurrence.selectedItem.toString()
            if (newRecurrence != originalRecurrence) updatedFields["recurrence"] = newRecurrence

            //Actializacion fecha y hora
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateParsed = try {
                dateFormat.parse(binding.etDate.text.toString())
            } catch (e: Exception) {
                null
            }
            if (dateParsed != null && dateParsed.time != originalDateMillis) {
                updatedFields["date"] =
                    com.google.firebase.Timestamp(dateParsed)
            }
            if (dateParsed == null) {
                Toast.makeText(
                    requireContext(),
                    "La fecha introducida no es válida",
                    Toast.LENGTH_SHORT
                ).show()
            }


            //Si no hay cambios que guardar
            if (updatedFields.isEmpty()) {
                Toast.makeText(requireContext(), "No hay cambios para guardar", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            //Verificacion de campos actualizados.
            Toast.makeText(
                requireContext(),
                "Campos actualizados: ${updatedFields.keys.joinToString()}",
                Toast.LENGTH_SHORT
            ).show()

            //Enviamos los datos al ViewModel
            val transactionId = args.getString("transactionId")
            if (transactionId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "ID de transacción inválido", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val isIncome = args.getBoolean("isIncome")

            if (isIncome) {
                val incomeFactory = IncomeViewModelFactory(IncomeRepository())
                val incomeViewModel =
                    ViewModelProvider(requireActivity(), incomeFactory)[IncomeViewModel::class.java]


                incomeViewModel.updateIncomeFields(transactionId, updatedFields) { success ->
                    if (success) {
                        //Actualizar lista de ingresos desde el ViewModel
                        val factory = TransactionsViewModelFactory(TransactionsRepository())
                        val parentViewModel =
                            ViewModelProvider(
                                requireActivity(),
                                factory
                            )[TransactionsViewModel::class.java]
                        parentViewModel.fetchIncomes()

                        Toast.makeText(
                            requireContext(),
                            "Ingreso actualizado con exito",
                            Toast.LENGTH_SHORT
                        ).show()
                        //Cierre del dialigo
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al actualizar el ingreso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } else {
                val expenseFactory =
                    ExpenseViewModelFactory(ExpenseRepository())
                val expenseViewModel =
                    ViewModelProvider(
                        requireActivity(),
                        expenseFactory
                    )[ExpenseViewModel::class.java]

                expenseViewModel.updateExpenseFields(transactionId, updatedFields) { sucess ->
                    if (sucess) {
                        //Actualizar lista de gastos desde el ViewModel
                        val factory = TransactionsViewModelFactory(TransactionsRepository())
                        val parentViewModel =
                            ViewModelProvider(
                                requireActivity(),
                                factory
                            )[TransactionsViewModel::class.java]
                        parentViewModel.fetchExpenses()

                        Toast.makeText(
                            requireContext(),
                            "Gasto actualizado con exito",
                            Toast.LENGTH_SHORT
                        ).show()
                        //Cierre del dialogo
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al actualizar el gasto",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}





