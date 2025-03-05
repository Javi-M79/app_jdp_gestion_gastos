package com.example.app_jdp_gestion_gastos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.app_jdp_gestion_gastos.databinding.ActivityRegistroBinding
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    //David he implementado binding en esta activity
    private lateinit var binding: ActivityRegistroBinding

    // Variable para acceder al ViewModel.
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Implemetacion de Binding.
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Incializacion de variables que interactuaran con el layout.
        val btnRegistrar = binding.btnRegistrar
        val tvVolverLogin = binding.tvVolverLogin

        // Vinculacion de variables con elementos del layout.
        /* val etMail = binding.etMailRegistro.text.toString().trim()
         val etPassword = binding.etPasswordRegistro.text.toString().trim()
         val etPasswordConfirm = binding.etPasswordConfirm.text.toString().trim()
         */


        // Manejo de registro de usuarios.
        btnRegistrar.setOnClickListener {
            //Variables que se pasaran por parametro a la funcion de registro.
            val email = binding.etMailRegistro.text.toString().trim()
            val password = binding.etPasswordRegistro.text.toString().trim()
            val confirmPassword = binding.etPasswordConfirm.text.toString().trim()

            //Validaciones de campos vacios.
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            //Validaciones de contraseñas.
            else if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Registro de usuario.
            else {
                userViewModel.registerUser(email, password, null) { userId, error ->
                    if (userId != null) {
                        Toast.makeText(this, "Usuario registrado con exito", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Boton para volver a la pantalla de login.
        tvVolverLogin.setOnClickListener {
            finish() // Cierra la actividad y vuelve al login
        }

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
