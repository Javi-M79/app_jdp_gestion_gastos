package com.example.app_jdp_gestion_gastos.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.app_jdp_gestion_gastos.databinding.DialogEditTransactionBinding
import java.util.Date

import java.text.SimpleDateFormat
import java.util.Calendar
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

        // Botón guardar  (implementar con Firebase)
        binding.btnSave.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Guardando cambios (falta implementar)",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


