package com.example.app_jdp_gestion_gastos.data.repository

import android.util.Log
import com.example.app_jdp_gestion_gastos.data.model.Group
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class GroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groupCollection = db.collection("groups")

    // Crear un nuevo grupo en Firestore
    suspend fun createGroup(group: Group): String? {
        return try {
            val docRef = groupCollection.add(group).await()
            docRef.update("id", docRef.id).await()
            docRef.id
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error al crear grupo", e)
            null
        }
    }

    // Obtener los grupos de un usuario en tiempo real
    fun getUserGroupsLive(userId: String, callback: (List<Group>) -> Unit) {
        groupCollection.whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GroupRepository", "Error obteniendo grupos en tiempo real", error)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val groups = it.documents.mapNotNull { doc ->
                        doc.toObject<Group>()?.apply { id = doc.id }
                    }
                    callback(groups)
                }
            }
    }

    // Obtener detalles de un grupo por su ID
    fun getGroupById(groupId: String, callback: (Group?) -> Unit) {
        groupCollection.document(groupId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.toObject(Group::class.java))
            }
            .addOnFailureListener {
                Log.e("GroupRepository", "Error al obtener grupo por ID", it)
                callback(null)
            }
    }

    // Agregar un usuario a un grupo existente
    suspend fun addUserToGroup(groupId: String, userId: String) {
        val groupRef = groupCollection.document(groupId)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(groupRef)
                val group = snapshot.toObject<Group>() ?: return@runTransaction

                val updatedMembers = group.members.toMutableSet()
                if (updatedMembers.add(userId)) {
                    transaction.update(groupRef, "members", updatedMembers.toList())
                }
            }.await()
            Log.d("GroupRepository", "Usuario $userId agregado al grupo $groupId")
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error al agregar usuario al grupo", e)
        }
    }

    // Actualizar la lista de miembros de un grupo
    fun updateGroupMembers(groupId: String, updatedMembers: List<String>, callback: (Boolean) -> Unit) {
        groupCollection.document(groupId)
            .update("members", updatedMembers)
            .addOnSuccessListener {
                Log.d("GroupRepository", "Miembros del grupo actualizados correctamente.")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("GroupRepository", "Error al actualizar los miembros del grupo", e)
                callback(false)
            }
    }

    // Eliminar un grupo por su ID
    suspend fun deleteGroup(groupId: String): Boolean {
        return try {
            val groupRef = groupCollection.document(groupId)
            val snapshot = groupRef.get().await()

            if (!snapshot.exists()) {
                Log.e("GroupRepository", "Intento de eliminar un grupo inexistente: $groupId")
                return false
            }

            groupRef.delete().await()
            Log.d("GroupRepository", "Grupo eliminado con éxito: $groupId")
            true
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error al eliminar grupo en Firestore", e)
            false
        }
    }
}