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
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserModelFactory

import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegistroFragment : Fragment() {

    // ViewBinding para acceder a las vistas del layout
    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!

    // by Lazy inicializa el repositorio solo cuando se necesita.
    private val userRepository by lazy {
        UserRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )
    }

    // Variable para acceder al ViewModel. by viewModels
    private val userViewModel: UserViewModel by viewModels {
        //Le pasamos el repositorio al ViewModel a traves del factory.
        UserModelFactory(userRepository)
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

        // Variables de los botones
        val btnRegistrar = binding.tvRegistro
        val tvVolverLogin = binding.tvVolverLogin

        btnRegistrar.setOnClickListener {
            //Variables que se pasaran por parametro a la funcion de registro.
            val mail = binding.etMailRegistro.text.toString().trim()
            val password = binding.etPasswordRegistro.text.toString().trim()
            val confirmPassword = binding.etPasswordConfirm.text.toString().trim()
            val name = binding.etNombreRegistro.text.toString().trim()

            //Una vez creadas las variables, las pasamos al viewmodel.
            userViewModel.registerUser(mail, password, confirmPassword, name) { userId, error ->

                // Si el usuario se ha registrado correctamente, se muestra un mensaje y se navega a la pantalla de login.
                if (userId != null) {
                    Toast.makeText(
                        requireContext(),
                        "Usuario registrado con exito",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        error ?: "Error desconocido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Navegación a la pantalla de login
        tvVolverLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}