package com.example.app_jdp_gestion_gastos.data.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable


//Modelo de la base de datos para usuario.
//get y set de propertyName. Notacion utilizada en firestore para traer el json de la coleccion y aplicarlo a las variables de la clase. Evita conflictos en los nombres
// si el nombre de las variables no coincide en la base de datos.
data class User(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("mail") @set:PropertyName("mail") var mail: String = "",
    @get:PropertyName("profile_picture") @set:PropertyName("profile_picture") var profilePicture: String = "",
    @get:PropertyName("groups") @set:PropertyName("groups") var groups: List<String> = emptyList(),
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long? = null,
    @get:PropertyName("balance") @set:PropertyName("balance") var balance: Double = 0.0,

    ) : Serializable

