package com.example.app_jdp_gestion_gastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_jdp_gestion_gastos.data.model.User
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import kotlinx.coroutines.launch


class UserViewModel(private val userRepository: UserRepository) : ViewModel() {


    //LOGIN
    fun loginUser(mail: String, password: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                //Llamamos al método loginUser del repositorio
                val userId = userRepository.loginUser(mail, password)
                onResult(
                    userId,
                    null
                ) // Si el inicio de sesión es exitoso, enviamos el ID del usuario y segundo parámetro null.

            } catch (e: Exception) {
                onResult(null, e.message)// Si el inicio de sesión falla, enviamos un mensaje de error.
            }
        }
    }


    // REGISTRO
    fun registerUser(
        mail: String,
        password: String,
        name: String? = null, //No se pide este dato en la pantalla de registro. Puede ser null. Pensar en la implementacion.
        onResult: (String?, String?) -> Unit // Callback para notificar el resultado del registro. Recibe el ID del usuario si es exitoso, o un mensaje de error si falla.
    ) {
        //Creamos una corrutina para ejecutar la acción en segundo plano. (ViewModelScope)
        viewModelScope.launch {
            try {
                //Llamamos al método registerUser del repositorio
                val userId = userRepository.registerUser(
                    mail,
                    password,
                    name
                )
                onResult(
                    userId,
                    null
                ) // Si el registro es exitoso, enviamos el ID del usuario y segundo parámetro null.

            } catch (e: Exception) {
                onResult(null, e.message) // Si el registro falla, enviamos un mensaje de error
            }
        }
    }

    // DATOS DE USUARIO LOGEADO
    fun getCurrentUser(onResult: (User?, String?) -> Unit) {
        //Creamos una corrutina para ejecutar la acción en segundo plano. (ViewModelScope)
        viewModelScope.launch {
            try {
                //Llamamos al método getCurrentUser del repositorio
                val currentUser = userRepository.getCurrentUser()

                onResult(
                    currentUser,
                    null
                )
            } catch (e: Exception) {
                onResult(null, e.message)
            }
        }
    }

    //LOGOUT
    fun logout(onResult: () -> Unit) {
        userRepository.logout()
    }
}









