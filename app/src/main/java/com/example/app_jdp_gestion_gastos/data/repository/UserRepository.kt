package com.example.app_jdp_gestion_gastos.data.repository

import android.util.Log
import com.example.app_jdp_gestion_gastos.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


/*OPERACIONES USER.
    - Registro
    - Obtener datos de usuario autenticado.
    - Cerrar sesion.
    */


class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    //REGISTRO USUARIO
    //suspend: Funcion suspendida para la ejecucion de operaciones asincronas o en segundo plano.

    suspend fun registerUser(email: String, password: String, name: String): String? {
        return try {
            //Desde el reposotorio conectamos con el auth de firebase.

            //Crear usuario en Firebase Authentication.
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return null

            // Crear usuario en Firestore usando su UID como ID del documento
            val newUser = User(
                name = name,
                mail = email,
                profilePicture = "",
                balance = 0.0,
                groups = listOf(),
                createdAt = System.currentTimeMillis()
            )

            db.collection("users").document(userId).set(newUser).await()
            userId
        } catch (e: Exception) {
            throw Exception("Error al registrar usuario: ${e.message}")

        }
    }

    //LOGIN USUARIO
    suspend fun loginUser(email: String, password: String): String? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.uid ?: throw Exception("Usuario no encontrado.")
        } catch (e: Exception) {
            throw Exception("Error al iniciar sesión: ${e.message}")
        }

    }


    //Obtener datos de usuario autenticado

    suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val documentSnapshot = db.collection("users").document(userId).get().await()
            if (!documentSnapshot.exists()) {
                // Recuperar nombre de usuario desde Firebase Authentication.
                var userName = documentSnapshot.getString("name")!!.toString()
                //Lanzamos una excepcion si ocurre un error al obtener los datos del usuario y la mandamos al Viewmodel.
                throw Exception("EL usuario con ID ${userId} no existe.")

            }
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            //Lanzamos una excepcion si ocurre un error al obtener los datos del usuario y la mandamos al Viewmodel.
            throw Exception("Error al obtener datos de usuario: ${e.message}")
        }

    }

    //Cerrar sesion
    fun logout() {
        auth.signOut()
    }
}