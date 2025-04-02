package com.example.app_jdp_gestion_gastos.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentLoginBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserModelFactory
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

    //Inicio del UserViewModel que recibe el reposotory a traves de ViewModdel factory
    private val userViewModel: UserViewModel by viewModels {
        UserModelFactory(userRepository)
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
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.tvTitulo.startAnimation(fadeIn)

        // Aplicar la animación a otros elementos
        binding.etMail.startAnimation(fadeIn)
        binding.etPassword.startAnimation(fadeIn)
        binding.btnLogin.startAnimation(fadeIn)
        binding.ivFondo.startAnimation(fadeIn)
        binding.tvCrearCuenta.startAnimation(fadeIn)

        // Agregar animaciones de foco a los EditText
        applyFocusAnimations()


        //LOGIN EN FIREBASE
        binding.btnLogin.setOnClickListener {
            //Variables  necesarias para el login.
            val mail = binding.etMail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            //Metodo en el userViewModel
            userViewModel.loginUser(mail, password) { userId, error ->
                if (userId != null) {
                    Toast.makeText(requireContext(), "Bienvenido, $mail", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_loginFragment_to_statsFragment)
                } else {
                    Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        // Navegación a la pantalla de registro
        binding.tvCrearCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }
    }

    private fun applyFocusAnimations() {
        val fondo = binding.ivFondo

        binding.etMail.setOnFocusChangeListener { _, hasFocus ->
            fondo.startAnimation(createScaleAnimation(if (hasFocus) 1.0f else 1.2f, if (hasFocus) 1.2f else 1.0f))
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            fondo.startAnimation(createScaleAnimation(if (hasFocus) 1.0f else 1.2f, if (hasFocus) 1.2f else 1.0f))
        }
    }

    private fun createScaleAnimation(from: Float, to: Float): ScaleAnimation {
        return ScaleAnimation(
            from, to,
            from, to,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            fillAfter = true
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





