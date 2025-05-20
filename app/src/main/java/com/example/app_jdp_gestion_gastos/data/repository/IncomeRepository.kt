package com.example.app_jdp_gestion_gastos.data.repository

import android.util.Log
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IncomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val incomeCollection = db.collection("incomes")

    // Agregar un nuevo ingreso a Firestore
    suspend fun addIncome(income: Income): Boolean {
        return try {
            //Conexion con firebase
            incomeCollection.add(income).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Modificar ingresos en Firestore
    fun updateIncomeFields(
        incomeId: String,
        fieldsToUpdate: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {

        try {
            //Conexion con firebase
            FirebaseFirestore.getInstance()
                //Acceso a la coleccion
                .collection("incomes")
                //Acceso al documento por ID
                .document(incomeId)
                //Aplicar solo los campos modificados
                .update(fieldsToUpdate)
                //Exito
                .addOnSuccessListener {
                    onComplete(true)
                }
                //Fallo
                .addOnFailureListener {
                    onComplete(false)
                }

        }catch (e: Exception){
            Log.e("Firestore:", "Excepcion al actualizar el ingreso", e)
            onComplete(false)
        }


    }


    // Obtener ingresos de un usuario específico
    suspend fun getIncomesByUser(userId: String): List<Income> {
        return try {
            val snapshot = incomeCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { doc ->
                val income = doc.toObject(Income::class.java)
                income?.id = doc.id  //Se asigna el ID del documento Firestore
                income
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}