package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentRegistroBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.AppViewModelFactory
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 * Use the [RegistroFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!


    private val userRepository by lazy {
        UserRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )
    }

    // Variable para acceder al ViewModel.
    private val userViewModel: UserViewModel by viewModels {
        AppViewModelFactory(userRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRegistrar = binding.tvRegistro
        val tvVolverLogin = binding.tvVolverLogin

        btnRegistrar.setOnClickListener {

            //Creamos las variables necesarias

            val mail = binding.etMailRegistro.text.toString().trim()
            val password = binding.etPasswordRegistro.text.toString().trim()
            val confirmPassword = binding.etPasswordConfirm.text.toString().trim()
            val name = binding.etNombreRegistro.text.toString().trim()

            //Una vez creadas las variables, las pasamos al viewmodel.

            userViewModel.registerUser(mail, password, confirmPassword, name) { userId, error ->
                if (userId != null) {
                    Toast.makeText(
                        requireContext(),
                        "Usuario registrado con exito",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        error ?: "Error desconocido",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }


        }

        tvVolverLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




