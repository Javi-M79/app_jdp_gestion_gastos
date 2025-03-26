package com.example.app_jdp_gestion_gastos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Expense(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Double = 0.0,
    @get:PropertyName("date") @set:PropertyName("date") var date: Timestamp? = null,
    @get:PropertyName("isRecurring") @set:PropertyName("isRecurring") var isRecurring: Boolean = false,
    @get:PropertyName("recurrence") @set:PropertyName("recurrence") var recurrence: String = ""
) : Serializable