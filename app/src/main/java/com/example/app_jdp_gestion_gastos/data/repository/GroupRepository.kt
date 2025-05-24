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
    private val userCollection = db.collection("users")

    // Método auxiliar para obtener el userId dado un email
    private suspend fun getUserIdByEmail(email: String): String? {
        val snapshot = userCollection.whereEqualTo("email", email).get().await()
        return if (!snapshot.isEmpty) snapshot.documents[0].id else null
    }

    suspend fun createGroup(group: Group): String? {
        val docRef = groupCollection.add(group).await()
        val groupId = docRef.id
        docRef.update("id", groupId).await()

        // Actualizar el campo "groups" en cada usuario miembro
        group.members.forEach { userEmail ->
            val userId = getUserIdByEmail(userEmail)
            if (userId != null) {
                updateUserGroupList(userId, groupId)
            } else {
                Log.w("GroupRepository", "Usuario con email $userEmail no encontrado para actualizar grupos")
            }
        }

        return groupId
    }

    suspend fun getUserGroups(userEmail: String): List<Group> {
        val snapshot = groupCollection.whereArrayContains("members", userEmail).get().await()
        return snapshot.documents.mapNotNull { it.toObject<Group>() }
    }

    suspend fun addUserToGroup(groupId: String, userEmail: String) {
        val groupRef = groupCollection.document(groupId)
        val snapshot = groupRef.get().await()
        val group = snapshot.toObject<Group>()

        if (group != null) {
            val updatedMembers = group.members.toMutableList()
            if (!updatedMembers.contains(userEmail)) {
                updatedMembers.add(userEmail)
                groupRef.update("members", updatedMembers).await()

                val userId = getUserIdByEmail(userEmail)
                if (userId != null) {
                    updateUserGroupList(userId, groupId)
                } else {
                    Log.w("GroupRepository", "Usuario con email $userEmail no encontrado para añadir al grupo")
                }
            }
        }
    }

    private suspend fun updateUserGroupList(userId: String, groupId: String) {
        val userRef = userCollection.document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentGroups = snapshot.get("groups") as? List<String> ?: emptyList()
            if (!currentGroups.contains(groupId)) {
                val updatedGroups = currentGroups + groupId
                transaction.update(userRef, "groups", updatedGroups)
            }
        }.await()
    }

    suspend fun removeUserFromGroup(groupId: String, userEmail: String) {
        val groupRef = groupCollection.document(groupId)
        val snapshot = groupRef.get().await()
        val group = snapshot.toObject<Group>()

        if (group != null) {
            val updatedMembers = group.members.toMutableList()
            if (updatedMembers.contains(userEmail)) {
                updatedMembers.remove(userEmail)
                groupRef.update("members", updatedMembers).await()

                val userId = getUserIdByEmail(userEmail)
                if (userId != null) {
                    removeGroupFromUser(userId, groupId)
                } else {
                    Log.w("GroupRepository", "Usuario con email $userEmail no encontrado para eliminar del grupo")
                }
            }
        }
    }

    private suspend fun removeGroupFromUser(userId: String, groupId: String) {
        val userRef = userCollection.document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentGroups = snapshot.get("groups") as? List<String> ?: emptyList()
            if (currentGroups.contains(groupId)) {
                val updatedGroups = currentGroups.filter { it != groupId }
                transaction.update(userRef, "groups", updatedGroups)
            }
        }.await()
    }

    suspend fun deleteGroup(groupId: String) {
        val groupRef = groupCollection.document(groupId)
        val snapshot = groupRef.get().await()
        val group = snapshot.toObject<Group>()

        if (group != null) {
            group.members.forEach { userEmail ->
                val userId = getUserIdByEmail(userEmail)
                if (userId != null) {
                    removeGroupFromUser(userId, groupId)
                } else {
                    Log.w("GroupRepository", "Usuario con email $userEmail no encontrado para eliminar grupo")
                }
            }
            groupRef.delete().await()
        }
    }

    suspend fun getAllUsers(): List<User> {
        val snapshot = userCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject<User>() }
    }

    fun listenToUserGroups(userEmail: String, onGroupsChanged: (List<Group>) -> Unit) {
        groupCollection.whereArrayContains("members", userEmail)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    Log.e("GroupRepository", "Error escuchando grupos", error)
                    return@addSnapshotListener
                }
                val groups = snapshots.documents.mapNotNull { it.toObject(Group::class.java) }
                onGroupsChanged(groups)
            }
    }
}