package com.example.app_jdp_gestion_gastos.data.repository

import android.util.Log
import com.example.app_jdp_gestion_gastos.data.model.Group
import com.example.app_jdp_gestion_gastos.data.model.User
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class GroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groupCollection = db.collection("groups")
    private val userCollection = db.collection("users") // AÑADIDO

    // Crear un nuevo grupo en Firestore
    suspend fun createGroup(group: Group): String? {
        val docRef = groupCollection.add(group).await()
        docRef.update("id", docRef.id).await() // Guardar el ID del documento en el campo "id"
        return docRef.id
    }

    // Obtener los grupos de un usuario
    suspend fun getUserGroups(userId: String): List<Group> {
        val snapshot = groupCollection.whereArrayContains("members", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject<Group>() }
    }

    // Agregar un usuario a un grupo existente
    suspend fun addUserToGroup(groupId: String, userId: String) {
        val groupRef = groupCollection.document(groupId)
        val snapshot = groupRef.get().await()
        val group = snapshot.toObject<Group>()

        if (group != null) {
            val updatedMembers = group.members.toMutableList()
            if (!updatedMembers.contains(userId)) {
                updatedMembers.add(userId)
                groupRef.update("members", updatedMembers).await()
            }
        }
    }

    /// Obtener usuarios por lista de IDs
    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        val users = mutableListOf<User>()
        try {
            val snapshots = userCollection.whereIn(FieldPath.documentId(), userIds).get().await()
            snapshots.documents.forEach { snapshot ->
                snapshot.toObject(User::class.java)?.let {
                    it.id = snapshot.id // 👈 Asignar UID manualmente
                    users.add(it)
                }
            }
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error al obtener usuarios: ${e.message}", e)
        }
        Log.d("GroupRepository", "Usuarios obtenidos: ${users.size}")
        return users
    }

    // Obtener todos los usuarios
    suspend fun getAllUsers(): List<User> {
        val snapshot = userCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.apply {
                id = doc.id // 👈 Asignar UID manualmente
            }
        }
    }

    // Actualizar los miembros del grupo
    suspend fun updateGroupMembers(groupId: String, members: List<String>) {
        groupCollection.document(groupId).update("members", members).await()
    }
}