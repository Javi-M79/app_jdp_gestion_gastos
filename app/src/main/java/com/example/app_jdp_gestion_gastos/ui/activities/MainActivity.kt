package com.example.app_jdp_gestion_gastos.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import com.example.app_jdp_gestion_gastos.databinding.ActivityMainBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.LogoutDialogo
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), LogoutDialogo.onDialogoLogOutListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth  // Agregamos FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()  // Inicializamos FirebaseAuth

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Configuración de la navegación
        binding.btnNavegacion.setupWithNavController(navController)
        // Quita el tinte de los iconos y los muestra por defecto
        binding.btnNavegacion.itemIconTintList = null

        // Ocultar botones de navegación en pantallas Login y Register
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Si el destino es loginFragment o registroFragment no mostramos los botones
            if (destination.id == R.id.loginFragment || destination.id == R.id.registroFragment) {
                binding.btnNavegacion.visibility = View.GONE
                binding.tvSaludoUsuario.visibility = View.GONE
                binding.btnLogOut.visibility = View.GONE
                binding.btnSettings.visibility = View.GONE
            } else {
                binding.btnNavegacion.visibility = View.VISIBLE
                binding.tvSaludoUsuario.visibility = View.VISIBLE
                binding.btnLogOut.visibility = View.VISIBLE
                binding.btnSettings.visibility = View.VISIBLE

                // Mostrar correo del usuario
                mostrarCorreoUsuario()

                // Botón Logout
                binding.btnLogOut.setOnClickListener {
                    val dialogo: LogoutDialogo = LogoutDialogo()
                    dialogo.show(supportFragmentManager, null)
                }

                // Botón para abrir SettingsFragment
                binding.btnSettings.setOnClickListener {
                    // Navegar al SettingsFragment
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .build()
                    navController.navigate(R.id.settingsFragment, null, navOptions)
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

    override fun onResume() {
        super.onResume()
        mostrarCorreoUsuario() // Actualizar el correo al volver a la actividad
    }

    private fun mostrarCorreoUsuario() {
        val userViewModel = UserViewModel(UserRepository()) // Asegúrate de inyectar correctamente el repositorio
        userViewModel.getCurrentUserName { nombre ->
            runOnUiThread {
                binding.tvSaludoUsuario.text = if (!nombre.isNullOrEmpty()) {
                    "Hola, $nombre"
                } else {
                    "Usuario no encontrado"
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun dialogoLogOutSelected() {
        auth.signOut() // Cerrar sesión en Firebase
        navController.navigate(R.id.loginFragment) // Navegar al loginFragment
    }
}