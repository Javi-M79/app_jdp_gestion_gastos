package com.example.app_jdp_gestion_gastos.data.repository

import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val expenses = MutableStateFlow<List<Expense>>(emptyList())

    suspend fun fetchExpenses(userId: String) {
        val snapshot = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        val expensesList = snapshot.toObjects(Expense::class.java)
        expenses.value = expensesList
    }

    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        db.collection("expenses").add(expense)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}