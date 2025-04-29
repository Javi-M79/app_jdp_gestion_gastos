package com.example.app_jdp_gestion_gastos.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

// Modelo de la base de datos para usuario.
// get y set de PropertyName: se usa para mapear correctamente los nombres de campos en Firestore.
data class User(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("mail") @set:PropertyName("mail") var mail: String = "",
    @get:PropertyName("profile_picture") @set:PropertyName("profile_picture") var profilePicture: String = "",
    @get:PropertyName("groups") @set:PropertyName("groups") var groups: List<String> = emptyList(),
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long? = null,
    @get:PropertyName("balance") @set:PropertyName("balance") var balance: Double = 0.0
) : Serializable {
    // Este campo 'id' es excluido de Firestore y se usará solo localmente
    @get:Exclude @set:Exclude
    var id: String? = null
}