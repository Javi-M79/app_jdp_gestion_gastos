package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí es donde añades el listener para el botón
        binding.btnGoToTransactions.setOnClickListener {
            findNavController().navigate(R.id.transactionsFragment)  // Aquí navegas al fragmento de transacciones
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}