package com.example.app_jdp_gestion_gastos.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.databinding.ActivityMainBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.LogoutDialogo


class MainActivity : AppCompatActivity(), LogoutDialogo.onDialogoLogOutListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController


        // Cofiguracion de la navegacion
        binding.btnNavegacion.setupWithNavController(navController)

        //Ocultar botones de navegacion en pantallas Login y Register
        navController.addOnDestinationChangedListener { _, destination, _ ->
            //Si el destino es loginFragment o Registro no mostramos los botones.
            if (destination.id == R.id.loginFragment || destination.id == R.id.registroFragment) {
                binding.btnNavegacion.visibility = View.GONE
                binding.tvSaludoUsuario.visibility = View.GONE
                binding.btnLogOut.visibility = View.GONE

            } else {
                binding.btnNavegacion.visibility = View.VISIBLE
                binding.tvSaludoUsuario.visibility = View.VISIBLE
                binding.btnLogOut.visibility = View.VISIBLE


                // Boton Logout
                binding.btnLogOut.setOnClickListener {
                    val dialogo: LogoutDialogo = LogoutDialogo()
                    dialogo.show(supportFragmentManager, null)
                }
            }
        }

        // TODO: Activar modo inmersivo (Desactivar barra de estado)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun dialogoLogOutSelected() {
        finish()
    }

}


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

// ACCESO PARA TEST
/*if (email == "admin" && password == "admin") {
    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
    val intent = Intent(applicationContext, HomeActivity::class.java)
    intent.putExtra("usuario", email)
    startActivity(intent)
}*/