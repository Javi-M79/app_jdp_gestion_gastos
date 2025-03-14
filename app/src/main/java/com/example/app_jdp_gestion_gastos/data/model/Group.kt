package com.example.app_jdp_gestion_gastos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Group(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("createdBy") @set:PropertyName("createdBy") var createdBy: String = "",
    @get:PropertyName("members") @set:PropertyName("members") var members: List<String> = emptyList(),
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null,
    @get:PropertyName("expenses") @set:PropertyName("expenses") var expenses: List<String> = emptyList(),
)
