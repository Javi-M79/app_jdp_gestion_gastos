package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentLoginBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.AppViewModelFactory
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    //Inicio del UserRepository
    private val userRepository by lazy {
        UserRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        )

    }

    //Inicio del UserViewModel que recibe el reposotory a traves de ViewmOdel factori
    private val userViewModel: UserViewModel by viewModels {
        AppViewModelFactory(userRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: VARIABLES
        // Animación del titulo
        val titulo = binding.tvTitulo
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        titulo.startAnimation(fadeIn)


        val login = binding.btnLogin
        val resetPassword = binding.resetpassword
        val fondo = binding.ivFondo
        val crearCuenta = binding.tvCrearCuenta


        //LOGIN EN FIREBASE
        login.setOnClickListener {
            //Variables  necesarias para el login.
            val mail = binding.etMail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            //Metodo en el userViewModel
            userViewModel.loginUser(mail, password) { userId, error ->
                if (userId != null) {
                    findNavController().navigate(R.id.action_loginFragment_to_statsFragment)
                } else {
                    Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

        crearCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*EVENTOS FOCUS MAIL Y PASWORD
etMail.setOnFocusChangeListener { _, hasFocus ->
    if (hasFocus) {
        // Zoom In
        val zoomIn = ScaleAnimation(
            1.0f, 1.2f, // Escala en X
            1.0f, 1.2f, // Escala en Y
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300 // Duración en milisegundos
            fillAfter = true // mantien el estado final
        }
        fondoImagen.startAnimation(zoomIn)
    } else {
        // Zoom Out
        val zoomOut = ScaleAnimation(
            1.2f, 1.0f,
            1.2f, 1.0f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            fillAfter = true
        }
        fondoImagen.startAnimation(zoomOut)
    }
}
etPassword.setOnFocusChangeListener { _, hasFocus ->
    if (hasFocus) {
        // Zoom In
        val zoomIn = ScaleAnimation(
            1.0f, 1.2f, // Escala en X
            1.0f, 1.2f, // Escala en Y
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300 // Duración en milisegundos
            fillAfter = true // mantien el estado final
        }
        fondoImagen.startAnimation(zoomIn)
    } else {
        // Zoom Out
        val zoomOut = ScaleAnimation(
            1.2f, 1.0f,
            1.2f, 1.0f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            fillAfter = true
        }
        fondoImagen.startAnimation(zoomOut)
    }
}*/





