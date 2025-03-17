package com.example.app_jdp_gestion_gastos.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_jdp_gestion_gastos.data.model.Group
import com.example.app_jdp_gestion_gastos.data.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {

    private val repository = GroupRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _userGroups = MutableLiveData<List<Group>>()
    val userGroups: LiveData<List<Group>> get() = _userGroups

    private val _selectedGroup = MutableLiveData<Group?>()
    val selectedGroup: LiveData<Group?> get() = _selectedGroup

    init {
        observeUserGroups()
    }

    private fun observeUserGroups() {
        val userId = getCurrentUserId()
        if (userId.isNotBlank()) {
            repository.getUserGroupsLive(userId) { groups ->
                _userGroups.postValue(groups)
                if (_selectedGroup.value?.let { !groups.contains(it) } == true) {
                    _selectedGroup.postValue(null)
                }
            }
        }
    }

    fun createGroup(name: String) {
        val userId = getCurrentUserId()
        if (userId.isBlank()) return

        viewModelScope.launch {
            val existingGroups = userGroups.value ?: emptyList()
            if (existingGroups.any { it.name == name }) {
                Log.e("GroupViewModel", "El grupo '$name' ya existe.")
                return@launch
            }

            val newGroup = Group(
                id = "",
                name = name,
                createdBy = userId,
                members = listOf(userId),
                createdAt = com.google.firebase.Timestamp.now()
            )

            repository.createGroup(newGroup)?.let { groupId ->
                Log.d("GroupViewModel", "Grupo creado exitosamente con ID: $groupId")
            } ?: Log.e("GroupViewModel", "Error al crear grupo: Firestore no devolvió ID")
        }
    }

    fun deleteSelectedGroup() {
        val group = _selectedGroup.value ?: return
        if (group.id.isBlank()) {
            Log.e("GroupViewModel", "Error: El ID del grupo es nulo o vacío")
            return
        }

        val userId = getCurrentUserId()
        if (group.createdBy != userId) {
            Log.e("GroupViewModel", "Error: Solo el creador del grupo puede eliminarlo")
            return
        }

        viewModelScope.launch {
            if (repository.deleteGroup(group.id)) {
                _selectedGroup.postValue(null)
                Log.d("GroupViewModel", "Grupo eliminado correctamente")
            } else {
                Log.e("GroupViewModel", "Error al eliminar el grupo")
            }
        }
    }

    fun selectGroup(group: Group) {
        _selectedGroup.value = group
        Log.d("GroupViewModel", "Grupo seleccionado: ${group.name}")
    }

    fun leaveGroup() {
        val group = _selectedGroup.value ?: return
        val userId = getCurrentUserId()

        if (group.members.contains(userId)) {
            val updatedMembers = group.members.toMutableList().apply {
                remove(userId)
            }

            viewModelScope.launch {
                repository.updateGroupMembers(group.id, updatedMembers) { success ->
                    if (success) {
                        _selectedGroup.postValue(null)
                        Log.d("GroupViewModel", "Usuario $userId salió del grupo ${group.name}")
                    } else {
                        Log.e("GroupViewModel", "Error al salir del grupo")
                    }
                }
            }
        }
    }

    fun loadGroupDetails(groupId: String) {
        repository.getGroupById(groupId) { group ->
            _selectedGroup.postValue(group)
        }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid.orEmpty()
    }
}
