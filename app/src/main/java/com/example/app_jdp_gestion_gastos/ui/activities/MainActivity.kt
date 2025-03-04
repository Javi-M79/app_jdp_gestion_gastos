package com.example.app_jdp_gestion_gastos.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🔥 Inicializa Firebase ANTES de cualquier otra acción
        FirebaseApp.initializeApp(this)

        // 🔥 Ahora podemos inicializar Firestore
        db = FirebaseFirestore.getInstance()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Prueba listado de usuarios.
        db.collection("users").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombre") ?: "Sin nombre"
                    val email = document.getString("email") ?: "Sin email"
                    val uid = document.getString("uid") ?: "Sin UID"

                    Log.d("Firestore", "Usuario: $uid => Nombre: $nombre, Email: $email")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al obtener usuarios", exception)
            }

        // TODO: Activar modo inmersivo (Desactivar barra de estado)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )


        // TODO: VARIABLES
        // Animación del titulo
        val titulo = findViewById<TextView>(R.id.tvTitulo)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        titulo.startAnimation(fadeIn)
        val etMail = findViewById<EditText>(R.id.etMail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvOlvidarPassword = findViewById<TextView>(R.id.tvOlvidarContraseña)
        val fondoImagen = findViewById<ImageView>(R.id.ivFondo)

        // TODO: LOGICA
        // Eventos de boton login
        btnLogin.setOnClickListener {
            val email = etMail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                loginUser(email, password)
            }
        }

        // Evento de botón para recuperar contraseña
        tvOlvidarPassword.setOnClickListener {
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
        }

        // Evento de zoom en el fondo Mail
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
        }

    }

    private fun loginUser(email: String, password: String) {
        // TODO: FIREBASE
        /*auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {task ->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show()
                }
            }*/

        // TODO: NORMAL
        if (email == "admin" && password == "admin") {
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.putExtra("usuario", email)
            startActivity(intent)
        }
    }

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
    }
}
