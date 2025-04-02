package com.example.app_jdp_gestion_gastos.data.repository

import com.example.app_jdp_gestion_gastos.data.model.Income
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IncomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val incomeCollection = db.collection("incomes")

    // Agregar un nuevo ingreso a Firestore
    suspend fun addIncome(income: Income): Boolean {
        return try {
            incomeCollection.add(income).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Modificar ingresos en Firestore


    // Obtener ingresos de un usuario específico
    suspend fun getIncomesByUser(userId: String): List<Income> {
        return try {
            val snapshot = incomeCollection.whereEqualTo("userId", userId).get().await()
            snapshot.toObjects(Income::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}