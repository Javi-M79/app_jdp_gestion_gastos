package com.example.app_jdp_gestion_gastos.data.repository

import com.example.app_jdp_gestion_gastos.data.model.Group
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class GroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groupCollection = db.collection("groups")

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
}