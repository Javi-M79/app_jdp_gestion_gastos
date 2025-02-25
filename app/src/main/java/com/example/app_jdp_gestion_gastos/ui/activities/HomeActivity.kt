package com.example.app_jdp_gestion_gastos.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.databinding.ActivityHomeBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.LogoutDialogo
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), LogoutDialogo.onDialogoLogOutListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mostramos el correo del usuario en el Activity
        val saludoUsuario = intent.getStringExtra("usuario") ?: "Default"
        binding.tvSaludoUsuario.text = "Bienvenido, $saludoUsuario"

        // Configurar Navigation Component con BottomNavigationView
        binding.btnNavegacion.itemIconTintList = null

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Asignar BottomNavigationView al controlador de navegación
        val bottomNavView: BottomNavigationView = binding.btnNavegacion
        bottomNavView.setupWithNavController(navController)

        // TODO: BOTON LOGOUT
        binding.btnLogOut.setOnClickListener{
            val dialogo: LogoutDialogo = LogoutDialogo()
            dialogo.show(supportFragmentManager, null)
            // TODO: CERRAR LA APLICACIÓN AL HACER CLICK EN EL BOTÓN LOGOUT
        }
    }

    // Implementar el listener para el diálogo de logout
    override fun dialogoLogOutSelected() {
        finish()
    }
}