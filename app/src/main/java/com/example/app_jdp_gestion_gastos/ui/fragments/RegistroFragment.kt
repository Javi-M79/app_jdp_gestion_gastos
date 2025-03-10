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


class RegistroFragment : Fragment() {

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
            //Variables que se pasaran por parametro a la funcion de registro.
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

        tvVolverLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registroFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



//NOTAS DAVID:
/*
//La conexion a la base de datos ahora se realiza con el ViewModel y el repository. Desvinculamos la UI de Firebase.
// Hay que pensar si tambien pedimos nombre de usuario en el registro.
auth = FirebaseAuth.getInstance()

val etMail = findViewById<EditText>(R.id.etMailRegistro)
val etPassword = findViewById<EditText>(R.id.etPasswordRegistro)
val etPasswordConfirm = findViewById<EditText>(R.id.etPasswordConfirm)
val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

 */


//Codigo de David para el registro de usuarios. MODIFICADO MAS ARRIBA USANDO VIEWMODEL
/*btnRegistrar.setOnClickListener {
    val email = etMail.text.toString().trim()
    val password = etPassword.text.toString().trim()
    val confirmPassword = etPasswordConfirm.text.toString().trim()

    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
            .show()
    } else if (password != confirmPassword) {
        Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
    } else {
        registrarUsuario(email, password)
    }
}*/

/*private fun registrarUsuario(email: String, password: String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }
}*/



