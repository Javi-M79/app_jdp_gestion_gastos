package com.example.app_jdp_gestion_gastos.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.app_jdp_gestion_gastos.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Cargar moneda
        val currencies = arrayOf("USD ($)", "EUR (€)", "MXN ($)", "COP ($)", "ARS ($)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencies)
        binding.spMoneda.adapter = adapter
        val savedCurrency = sharedPreferences.getString("currency", "USD ($)")
        binding.spMoneda.setSelection(currencies.indexOf(savedCurrency))

        binding.spMoneda.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putString("currency", currencies[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Modo oscuro
        binding.switchDarkMode.isChecked = sharedPreferences.getBoolean("darkMode", false)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Presupuesto mensual
        binding.btnSaveBudget.setOnClickListener {
            val budget = binding.etPresupuesto.text.toString()
            if (budget.isNotEmpty()) {
                sharedPreferences.edit().putFloat("monthlyBudget", budget.toFloat()).apply()
                Toast.makeText(requireContext(), "Presupuesto guardado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Ingrese un presupuesto", Toast.LENGTH_SHORT).show()
            }
        }

        // Restablecer datos
        binding.btnResetData.setOnClickListener {
            sharedPreferences.edit().remove("transactions").apply()
            Toast.makeText(requireContext(), "Datos restablecidos", Toast.LENGTH_SHORT).show()
        }

        // Notificaciones
        binding.switchNotificaciones.isChecked = sharedPreferences.getBoolean("notifications", true)
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications", isChecked).apply()
            Toast.makeText(requireContext(), if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
        }

        // Cambio de idioma
        val languages = arrayOf("Español", "Inglés")
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        binding.spIdioma.adapter = languageAdapter
        val savedLanguage = sharedPreferences.getString("language", "Español")
        binding.spIdioma.setSelection(languages.indexOf(savedLanguage))

        binding.spIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putString("language", languages[position]).apply()
                Toast.makeText(requireContext(), "Idioma cambiado a ${languages[position]}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Exportación de datos (Simulación)
        binding.btnExportData.setOnClickListener {
            Toast.makeText(requireContext(), "Exportando datos...", Toast.LENGTH_SHORT).show()
            // Aquí puedes agregar la lógica real para exportar datos
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}