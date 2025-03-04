package com.example.app_jdp_gestion_gastos.data.repository

import com.example.app_jdp_gestion_gastos.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


/*OPERACIONES USER.
    - Registro
    - Obtener datos de usuario autenticado.
    - Cerrar sesion.
    */


class UserRepository {

    //Inicio de firebase

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Registrar usuario en Firebase Authentication y Firestore.
    //suspend: Funcion suspendida para la ejecucion de operaciones asincronas o en segundo plano.
    suspend fun registerUser(email: String, password: String, name: String): String? {
        return try {
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
            println("Error al registrar usuario: ${e.message}")
            null
        }
    }

    //Obtener datos de usuario autenticado

    suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val documentSnapshot = db.collection("users").document(userId).get().await()
            if (!documentSnapshot.exists()) {
                println("EL usuario con ID ${userId} no existe.")
                return null
            }
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            println("Error al obtener datos de usuario: ${e.message}")
            null
        }

    }

    //Cerrar sesion
    fun logout() {
        auth.signOut()
    }
}