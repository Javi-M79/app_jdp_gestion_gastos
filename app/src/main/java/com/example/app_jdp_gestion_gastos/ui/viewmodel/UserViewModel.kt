package com.example.app_jdp_gestion_gastos.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_jdp_gestion_gastos.data.model.User
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import kotlinx.coroutines.launch


class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    //LOGIN
    fun loginUser(mail: String, password: String, onResult: (String?, String?) -> Unit) {

        //Comprobacion de que estan rellenos todos los campos

        if (mail.isEmpty() || password.isEmpty()) {
            onResult(null, "Por favor, completa todos los campos")
            return
        }
        //Corrutina para ejecutar la acción en segundo plano. (ViewModelScope)
        viewModelScope.launch {
            try {
                //Llamamos al método loginUser del repositorio
                val userId = userRepository.loginUser(mail, password)
                onResult(
                    userId,
                    null
                ) // Si el inicio de sesión es exitoso, enviamos el ID del usuario y segundo parámetro null.

            } catch (e: Exception) {
                onResult(
                    null,
                    e.message
                )// Si el inicio de sesión falla, enviamos un mensaje de error.
            }
        }
    }


    // REGISTRO
    fun registerUser(
        mail: String,
        password: String,
        confirmPassword: String,
        name: String,
        onResult: (String?, String?) -> Unit // Callback para notificar el resultado del registro. Recibe el ID del usuario si es exitoso, o un mensaje de error si falla.
    ) {
        //Validacion de datos
        if (name.isEmpty() || mail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            onResult(null, "Por favor, completa todos los campos")
            return
        }

        //Validacion de que las contraseñas coiciden
        if (password != confirmPassword) {
            onResult(null, "Las contraseñas no coinciden")
            return
        }
        //Corrutina para ejecutar la acción en segundo plano. (ViewModelScope)
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



