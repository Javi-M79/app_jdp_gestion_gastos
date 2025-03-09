package com.example.app_jdp_gestion_gastos.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController


        // TODO: Activar modo inmersivo (Desactivar barra de estado)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )


        /* // TODO: VARIABLES
         // Animación del titulo
         val titulo = findViewById<TextView>(R.id.tvTitulo)
         val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
         titulo.startAnimation(fadeIn)
         val etMail = findViewById<EditText>(R.id.etMail)
         val etPassword = findViewById<EditText>(R.id.etPassword)
         val btnLogin = findViewById<Button>(R.id.btnLogin)
         val tvOlvidarPassword = findViewById<TextView>(R.id.tvOlvidarContraseña)
         val fondoImagen = findViewById<ImageView>(R.id.ivFondo)
         val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)*/


        // Eventos de boton login
        /*btnLogin.setOnClickListener {
            val email = etMail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                loginUser(email, password)
            }
        }*/

        //TODO INCLUIR ESTE METODO EN EL VIEWMODEL
        // Evento de botón para recuperar contraseña
        /* tvOlvidarPassword.setOnClickListener {
             val email = etMail.text.toString().trim()
             if (email.isEmpty()) {
                 Toast.makeText(
                     this,
                     "Ingresa tu correo para recuperar la contraseña",
                     Toast.LENGTH_SHORT
                 ).show()
             } else {
                 resetPassword(email)
             }
         }*/


    }


    /* private fun loginUser(email: String, password: String) {
         // TODO: FIREBASE
         auth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this) { task ->
                 if (task.isSuccessful) {
                     Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                     val intent = Intent(applicationContext, HomeActivity::class.java)
                     intent.putExtra("usuario", email)
                     startActivity(intent)
                     // finish()
                 } else {
                     Toast.makeText(this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show()
                 }
             }

         // ACCESO PARA TEST
         /*if (email == "admin" && password == "admin") {
             Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
             val intent = Intent(applicationContext, HomeActivity::class.java)
             intent.putExtra("usuario", email)
             startActivity(intent)
         }*/
     }*/


    /* METODO RESET PASSWORD
     private fun resetPassword(email: String) {
         auth.sendPasswordResetEmail(email)
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     Toast.makeText(
                         this,
                         "Se ha enviado un correo de recuperación",
                         Toast.LENGTH_SHORT
                     ).show()
                 } else {
                     Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show()
                 }
             }
     }*/

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}